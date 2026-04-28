package com.mvi.kenny.feature.remotecompose

// ================================================================
// RemoteComposeContract — AndroidX Remote Compose 服务器驱动 UI 开发工具包 MVI Contract
// ================================================================
// MVI architecture contract for AndroidX Remote Compose developer toolkit.
//
// PRD-191: AndroidX Remote Compose 服务器驱动 UI 开发工具包
// Design Reference: memory/agency/designs/PRD-191-AndroidX-Remote-Compose-开发工具包.md
//
// This toolkit provides 10 developer tools:
//   1. Remote Compose Integration Template (client-side RemoteComposePlayer)
//   2. Remote Compose Server DSL Guide (Kotlin DSL writing spec)
//   3. Remote Compose vs JSON SDUI Decision Tree
//   4. Remote Compose Security Guide (payload signing/verification)
//   5. Remote Compose CI/CD Template (server-side Compose DSL testing)
//   6. Remote Compose × A/B Testing Integration Template
//   7. Remote Compose Accessibility Guide (TalkBack compatibility)
//   8. Remote Compose Monitoring & Observability Tools
//   9. Remote Compose Fallback Strategy Template
//  10. Remote Compose Migration Path (JSON SDUI → Remote Compose)
//
// MVI Three Pillars / MVI 三要素:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Section — 工具包模块枚举
// ================================================================

/**
 * ============================================================
 * RemoteComposeSection — 工具包模块枚举
 * ============================================================
 * Represents each module/section in the Remote Compose toolkit.
 * Each section is a self-contained developer tool.
 *
 * @property titleCn 中文标题
 * @property titleEn English title
 * @property emoji Emoji representation
 * @property description Brief description of the tool
 * @property priority Display priority (lower = first)
 */
enum class RemoteComposeSection(
    val titleCn: String,
    val titleEn: String,
    val emoji: String,
    val description: String,
    val priority: Int
) {
    // ── 客户端模块 ──
    CLIENT_INTEGRATION(
        titleCn = "客户端集成模板",
        titleEn = "Client Integration Template",
        emoji = "📱",
        description = "RemoteComposePlayer 完整配置 + Gradle 依赖 + 最小可运行示例",
        priority = 1
    ),

    // ── 服务器端模块 ──
    SERVER_DSL_GUIDE(
        titleCn = "服务器端 DSL 指南",
        titleEn = "Server DSL Guide",
        emoji = "⚙️",
        description = "Kotlin DSL 编写规范 + Payload 结构设计",
        priority = 2
    ),

    // ── 选型决策 ──
    DECISION_TREE(
        titleCn = "选型决策树",
        titleEn = "Decision Tree",
        emoji = "🌲",
        description = "Remote Compose vs JSON Server-Driven UI 场景对比和选型依据",
        priority = 3
    ),

    // ── 安全模块 ──
    SECURITY_GUIDE(
        titleCn = "安全防护指南",
        titleEn = "Security Guide",
        emoji = "🔐",
        description = "Payload 签名/加密/验证 + 注入攻击防御",
        priority = 4
    ),

    // ── CI/CD 模块 ──
    CI_CD_TEMPLATE(
        titleCn = "CI/CD 模板",
        titleEn = "CI/CD Template",
        emoji = "🚀",
        description = "GitHub Actions 服务器端 Compose DSL 测试 + Payload 大小基准测试",
        priority = 5
    ),

    // ── A/B 测试模块 ──
    AB_TESTING(
        titleCn = "A/B Testing 集成",
        titleEn = "A/B Testing Integration",
        emoji = "🧪",
        description = "服务端实时切换 UI 变体的完整工作流",
        priority = 6
    ),

    // ── Accessibility 模块 ──
    ACCESSIBILITY(
        titleCn = "Accessibility 适配",
        titleEn = "Accessibility Guide",
        emoji = "♿",
        description = "TalkBack 与动态绘制内容的协同规范",
        priority = 7
    ),

    // ── 可观测性模块 ──
    MONITORING(
        titleCn = "监控与可观测性",
        titleEn = "Monitoring & Observability",
        emoji = "📊",
        description = "渲染成功率/延迟/崩溃率监控仪表盘配置",
        priority = 8
    ),

    // ── 降级策略模块 ──
    FALLBACK_STRATEGY(
        titleCn = "降级策略模板",
        titleEn = "Fallback Strategy",
        emoji = "🛟",
        description = "网络失败/服务器异常时的优雅降级 UX",
        priority = 9
    ),

    // ── 迁移路径模块 ──
    MIGRATION_PATH(
        titleCn = "迁移路径",
        titleEn = "Migration Path",
        emoji = "📦",
        description = "从 JSON Server-Driven UI 迁移到 Remote Compose 的完整步骤",
        priority = 10
    )
}

// ================================================================
// Tool — 单个工具卡片
// ================================================================

/**
 * ============================================================
 * RemoteComposeTool — 工具卡片数据模型
 * ============================================================
 * Represents a single tool within a section.
 *
 * @property id Unique identifier for the tool
 * @property nameCn 中文名称
 * @property nameEn English name
 * @property codeTemplate Code template content (Kotlin/Java/XML)
 * @property language Programming language of the template
 * @property annotations Inline annotations explaining key lines
 * @property isBookmarked Whether user has bookmarked this tool
 */
data class RemoteComposeTool(
    val id: String,
    val nameCn: String,
    val nameEn: String,
    val codeTemplate: String,
    val language: CodeLanguage,
    val annotations: List<CodeAnnotation> = emptyList(),
    val isBookmarked: Boolean = false
)

/**
 * ============================================================
 * CodeLanguage — 编程语言枚举
 * ============================================================
 */
enum class CodeLanguage(val displayName: String, val extension: String) {
    KOTLIN("Kotlin", ".kt"),
    JAVA("Java", ".java"),
    XML("XML", ".xml"),
    GRADLE("Gradle", ".gradle.kts"),
    YAML("YAML", ".yml"),
    MARKDOWN("Markdown", ".md"),
    KOTLIN_SCRIPT("Kotlin Script", ".kts")
}

/**
 * ============================================================
 * CodeAnnotation — 代码注释标注
 * ============================================================
 * Represents an annotation on a specific line of code.
 *
 * @property lineNumber Line number to annotate
 * @property text Annotation text
 * @property type Annotation type (warning, tip, version, important)
 */
data class CodeAnnotation(
    val lineNumber: Int,
    val text: String,
    val type: AnnotationType = AnnotationType.TIP
)

/**
 * ============================================================
 * AnnotationType — 注释标注类型
 * ============================================================
 */
enum class AnnotationType(val emoji: String, val color: Color) {
    WARNING("⚠️", Color(0xFFFFD700)),    // Yellow - attention needed
    TIP("💡", Color(0xFF58A6FF)),        // Blue - helpful tip
    VERSION("📌", Color(0xFFA371F7)),    // Purple - version-specific
    IMPORTANT("🔥", Color(0xFFF85149)), // Red - critical info
    SUCCESS("✅", Color(0xFF3FB950))      // Green - confirmed/working
}

// ================================================================
// Payload Status — Payload 下载状态
// ================================================================

/**
 * ============================================================
 * PayloadStatus — Payload 状态枚举
 * ============================================================
 * Represents the current status of a Skia binary payload.
 *
 * Used when demonstrating RemoteComposePlayer behavior.
 */
enum class PayloadStatus {
    IDLE,           // 初始空闲状态
    DOWNLOADING,    // 正在下载 Payload
    READY,          // Payload 就绪，可渲染
    ERROR,          // 下载/解析出错
    DEPRECATED      // Payload 版本过旧
}

/**
 * ============================================================
 * RenderError — 渲染错误类型
 * ============================================================
 */
enum class RenderError(
    val code: String,
    val displayNameCn: String,
    val displayNameEn: String,
    val suggestion: String
) {
    VERSION_MISMATCH(
        code = "ERR_VERSION_MISMATCH",
        displayNameCn = "版本不兼容",
        displayNameEn = "Version Mismatch",
        suggestion = "客户端 alpha06 与服务器下发的 Payload 版本不兼容，请确认服务器下发兼容版本"
    ),
    PAYLOAD_CORRUPT(
        code = "ERR_PAYLOAD_CORRUPT",
        displayNameCn = "Payload 损坏",
        displayNameEn = "Payload Corrupted",
        suggestion = "Skia 二进制 Payload 损坏，请检查服务器序列化过程和网络传输"
    ),
    NETWORK_ERROR(
        code = "ERR_NETWORK_ERROR",
        displayNameCn = "网络错误",
        displayNameEn = "Network Error",
        suggestion = "Payload 下载失败，请检查网络连接并重试"
    ),
    RENDER_CRASH(
        code = "ERR_RENDER_CRASH",
        displayNameCn = "渲染崩溃",
        displayNameEn = "Render Crash",
        suggestion = "RemoteComposePlayer 渲染崩溃，请检查 Payload 内容和客户端版本"
    )
}

// ================================================================
// State — MVI State
// ================================================================

/**
 * ============================================================
 * RemoteComposeState — MVI State
 * ============================================================
 * Immutable state representing the Remote Compose toolkit's current status.
 *
 * @property selectedSection Currently selected toolkit section
 * @property expandedSections Set of sections that are expanded
 * @property selectedTool Current tool being viewed (null = section overview)
 * @property payloadStatus Simulated payload download status (for demo)
 * @property codeViewerContent Content being shown in code viewer
 * @property codeViewerLanguage Language of current code viewer content
 * @property searchQuery Search query for filtering tools
 * @property isLoading Whether a code template is being loaded
 * @property errorMessage Error message if any operation failed
 * @property activeTab Current main tab (tools/decision/fallback)
 */
data class RemoteComposeState(
    val selectedSection: RemoteComposeSection = RemoteComposeSection.CLIENT_INTEGRATION,
    val expandedSections: Set<RemoteComposeSection> = setOf(RemoteComposeSection.CLIENT_INTEGRATION),
    val selectedTool: RemoteComposeTool? = null,
    val payloadStatus: PayloadStatus = PayloadStatus.IDLE,
    val codeViewerContent: String = "",
    val codeViewerLanguage: CodeLanguage = CodeLanguage.KOTLIN,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeTab: MainTab = MainTab.TOOLS
) {
    /**
     * 当前选中模块的工具列表
     * Tools available in the currently selected section
     */
    val toolsForSelectedSection: List<RemoteComposeTool>
        get() = getToolsForSection(selectedSection)

    /**
     * 是否显示代码查看器
     */
    val isCodeViewerVisible: Boolean
        get() = codeViewerContent.isNotEmpty()
}

/**
 * ============================================================
 * MainTab — 主 Tab 枚举
 * ============================================================
 */
enum class MainTab(val titleCn: String, val titleEn: String) {
    TOOLS("工具集", "Tools"),
    DECISION("选型树", "Decision"),
    FALLBACK("降级策略", "Fallback")
}

// ================================================================
// Intent — MVI Intent
// ================================================================

/**
 * ============================================================
 * RemoteComposeIntent — MVI Intent
 * ============================================================
 * User intentions that the ViewModel processes.
 * 用户意图，ViewModel 处理并更新 State。
 */
sealed class RemoteComposeIntent {
    /**
     * 选择模块
     * Select a toolkit section
     */
    data class SelectSection(val section: RemoteComposeSection) : RemoteComposeIntent()

    /**
     * 展开/折叠模块
     * Toggle section expansion
     */
    data class ToggleSection(val section: RemoteComposeSection) : RemoteComposeIntent()

    /**
     * 选择工具查看详情
     * Select a tool to view its code template
     */
    data class SelectTool(val tool: RemoteComposeTool) : RemoteComposeIntent()

    /**
     * 关闭代码查看器
     * Close the code viewer
     */
    data object CloseCodeViewer : RemoteComposeIntent()

    /**
     * 切换主 Tab
     * Switch main tab
     */
    data class SwitchTab(val tab: MainTab) : RemoteComposeIntent()

    /**
     * 复制代码到剪贴板
     * Copy code template to clipboard
     */
    data class CopyCode(val code: String) : RemoteComposeIntent()

    /**
     * 更新搜索查询
     * Update search query
     */
    data class UpdateSearch(val query: String) : RemoteComposeIntent()

    /**
     * 收藏/取消收藏工具
     * Bookmark or unbookmark a tool
     */
    data class ToggleBookmark(val toolId: String) : RemoteComposeIntent()

    /**
     * 模拟 Payload 下载（演示用）
     * Simulate payload download for demo
     */
    data class SimulatePayloadDownload(val url: String) : RemoteComposeIntent()

    /**
     * 重置错误消息
     * Dismiss error message
     */
    data object DismissError : RemoteComposeIntent()
}

// ================================================================
// Effect — MVI Effect
// ================================================================

/**
 * ============================================================
 * RemoteComposeEffect — MVI Effect
 * ============================================================
 * One-time side effects emitted via Channel.
 * 一次性副作用，通过 Channel 发射。
 */
sealed class RemoteComposeEffect {
    /**
     * 显示 Toast 消息
     * Show a toast message
     */
    data class ShowToast(
        val message: String,
        val isSuccess: Boolean = true
    ) : RemoteComposeEffect()

    /**
     * 代码已复制到剪贴板
     * Code copied to clipboard
     */
    data object CodeCopied : RemoteComposeEffect()

    /**
     * 导航到外部文档
     * Navigate to external documentation
     */
    data class OpenExternalDoc(val url: String) : RemoteComposeEffect()

    /**
     * 记录分析事件
     * Log analytics event
     */
    data class LogAnalytics(
        val event: String,
        val params: Map<String, String> = emptyMap()
    ) : RemoteComposeEffect()

    /**
     * 错误事件
     * Error event
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : RemoteComposeEffect()
}

// ================================================================
// Tool Definitions — 工具定义（设计文档第 3 节模块结构）
// ================================================================

/**
 * ============================================================
 * getToolsForSection — 获取指定模块的工具列表
 * ============================================================
 * Returns the list of tools for a given section based on the design document.
 * Returns pre-populated code templates for each tool.
 */
fun getToolsForSection(section: RemoteComposeSection): List<RemoteComposeTool> {
    return when (section) {
        RemoteComposeSection.CLIENT_INTEGRATION -> listOf(
            RemoteComposeTool(
                id = "client_player_template",
                nameCn = "RemoteComposePlayer 集成模板",
                nameEn = "RemoteComposePlayer Integration Template",
                codeTemplate = CLIENT_PLAYER_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(8, "RemoteComposePlayer 是框架提供的核心渲染组件（RemoteComposePlayer.kt）", AnnotationType.TIP),
                    CodeAnnotation(12, "alpha06 版本必须指定，否则可能引入不兼容问题", AnnotationType.VERSION),
                    CodeAnnotation(18, "onRenderError 回调处理 Skia 渲染失败，必须实现降级逻辑", AnnotationType.IMPORTANT),
                    CodeAnnotation(22, "versionCode 用于版本协商，客户端上报支持的最大版本", AnnotationType.TIP)
                )
            ),
            RemoteComposeTool(
                id = "client_gradle_config",
                nameCn = "Gradle 依赖配置",
                nameEn = "Gradle Dependency Configuration",
                codeTemplate = GRADLE_CONFIG_TEMPLATE,
                language = CodeLanguage.GRADLE,
                annotations = listOf(
                    CodeAnnotation(3, "androidx.compose.remotecompose 仅在 alpha/beta 阶段可用", AnnotationType.WARNING),
                    CodeAnnotation(5, "MavenCentral 优先，Google Maven 作为备用源", AnnotationType.TIP),
                    CodeAnnotation(12, "Skia 二进制序列化需要 kotlinx-serialization", AnnotationType.IMPORTANT)
                )
            ),
            RemoteComposeTool(
                id = "client_example_activity",
                nameCn = "最小可运行示例",
                nameEn = "Minimal Working Example",
                codeTemplate = EXAMPLE_ACTIVITY_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(14, "示例演示：如何加载远程 Payload 并渲染 UI", AnnotationType.TIP),
                    CodeAnnotation(23, "fallback UI 使用现有 Material 3 主题，与框架风格一致", AnnotationType.SUCCESS)
                )
            )
        )

        RemoteComposeSection.SERVER_DSL_GUIDE -> listOf(
            RemoteComposeTool(
                id = "server_dsl_guide",
                nameCn = "服务器端 Composable 编写规范",
                nameEn = "Server Composable Writing Specification",
                codeTemplate = SERVER_DSL_GUIDE_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(6, "纯函数约束：Composable 必须是纯函数，无副作用，否则序列化失败", AnnotationType.WARNING),
                    CodeAnnotation(9, "@RemoteComposable 注解标记可序列化的 Composable 函数", AnnotationType.VERSION),
                    CodeAnnotation(14, "所有参数必须支持 kotlinx.serialization", AnnotationType.IMPORTANT),
                    CodeAnnotation(21, "Skia 二进制默认 gzip 压缩后传输，减少 Payload 大小", AnnotationType.TIP)
                )
            ),
            RemoteComposeTool(
                id = "payload_design_guide",
                nameCn = "Payload 结构设计指南",
                nameEn = "Payload Structure Design Guide",
                codeTemplate = PAYLOAD_DESIGN_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(5, "version 字段用于版本协商，客户端上报支持的最大版本", AnnotationType.IMPORTANT),
                    CodeAnnotation(11, "payload 经过 gzip 压缩，通常可减少 70-80% 大小", AnnotationType.TIP),
                    CodeAnnotation(15, "timestamp 用于缓存控制和版本判断", AnnotationType.VERSION)
                )
            ),
            RemoteComposeTool(
                id = "server_example_composable",
                nameCn = "服务器端 Composable 示例",
                nameEn = "Server Composable Example",
                codeTemplate = SERVER_EXAMPLE_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(7, "服务器端 Composable 使用纯 JVM Kotlin，无需 Android 依赖", AnnotationType.TIP),
                    CodeAnnotation(12, "序列化后的 Skia 二进制通过 HTTP 请求下发到客户端", AnnotationType.IMPORTANT)
                )
            )
        )

        RemoteComposeSection.DECISION_TREE -> listOf(
            RemoteComposeTool(
                id = "decision_tree_markdown",
                nameCn = "选型决策树（Markdown）",
                nameEn = "Decision Tree (Markdown)",
                codeTemplate = DECISION_TREE_MARKDOWN,
                language = CodeLanguage.MARKDOWN,
                annotations = listOf(
                    CodeAnnotation(1, "从「是否需要动态 UI」开始，逐步引导到最优方案", AnnotationType.TIP),
                    CodeAnnotation(8, "Remote Compose 适合：需要极致性能 + Compose 开发者体验的场景", AnnotationType.SUCCESS),
                    CodeAnnotation(14, "JSON SDUI 适合：已有后端基础设施 + 跨平台需求的场景", AnnotationType.TIP)
                )
            ),
            RemoteComposeTool(
                id = "decision_flow_chart",
                nameCn = "选型流程图（ASCII）",
                nameEn = "Decision Flow Chart (ASCII)",
                codeTemplate = DECISION_FLOW_CHART,
                language = CodeLanguage.MARKDOWN,
                annotations = listOf(
                    CodeAnnotation(1, "快速可视化决策路径，适合文档和幻灯片使用", AnnotationType.TIP)
                )
            )
        )

        RemoteComposeSection.SECURITY_GUIDE -> listOf(
            RemoteComposeTool(
                id = "payload_signing",
                nameCn = "Payload 签名机制",
                nameEn = "Payload Signing Mechanism",
                codeTemplate = PAYLOAD_SIGNING_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(5, "HMAC-SHA256 是 Google 推荐的 Payload 签名算法", AnnotationType.IMPORTANT),
                    CodeAnnotation(9, "签名密钥必须安全存储，不能硬编码或放在客户端", AnnotationType.WARNING),
                    CodeAnnotation(14, "时间戳验证防止重放攻击，Payload 有效期通常设为 5-10 分钟", AnnotationType.TIP)
                )
            ),
            RemoteComposeTool(
                id = "payload_verification",
                nameCn = "Payload 验签流程",
                nameEn = "Payload Verification Flow",
                codeTemplate = PAYLOAD_VERIFICATION_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(8, "验签失败时必须拒绝 Payload，展示 fallback UI", AnnotationType.IMPORTANT),
                    CodeAnnotation(12, "日志仅记录验签失败事件，不记录 Payload 内容（防止敏感信息泄露）", AnnotationType.WARNING)
                )
            ),
            RemoteComposeTool(
                id = "injection_defense",
                nameCn = "注入攻击防御指南",
                nameEn = "Injection Attack Defense Guide",
                codeTemplate = INJECTION_DEFENSE_TEMPLATE,
                language = CodeLanguage.MARKDOWN,
                annotations = listOf(
                    CodeAnnotation(1, "三大攻击向量：MITM / Content Injection / Payload Tampering", AnnotationType.IMPORTANT),
                    CodeAnnotation(7, "HTTPS + 证书固定防止 MITM，中间人无法解密 Payload", AnnotationType.SUCCESS),
                    CodeAnnotation(11, "HMAC 签名防止 Payload 被篡改或重放", AnnotationType.SUCCESS)
                )
            )
        )

        RemoteComposeSection.CI_CD_TEMPLATE -> listOf(
            RemoteComposeTool(
                id = "ci_pipeline",
                nameCn = "GitHub Actions DSL 测试 CI",
                nameEn = "GitHub Actions DSL Testing CI",
                codeTemplate = CI_PIPELINE_TEMPLATE,
                language = CodeLanguage.YAML,
                annotations = listOf(
                    CodeAnnotation(4, "使用 K2 编译器模式，远程 Composable DSL 需要 K2 支持", AnnotationType.VERSION),
                    CodeAnnotation(11, "服务器端 Compose DSL 使用纯 JVM 测试，无需 Android 模拟器", AnnotationType.TIP),
                    CodeAnnotation(18, "payload-size-check 限制单个 Payload 不超过 500KB（Gzip 后）", AnnotationType.WARNING)
                )
            ),
            RemoteComposeTool(
                id = "server_test_template",
                nameCn = "服务器端 Compose 单元测试模板",
                nameEn = "Server Compose Unit Test Template",
                codeTemplate = SERVER_TEST_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(5, "测试纯度：验证 Composable 是否为纯函数（无副作用）", AnnotationType.TIP),
                    CodeAnnotation(12, "序列化测试：验证 Composable 结果可正确序列化为 Skia 二进制", AnnotationType.IMPORTANT)
                )
            ),
            RemoteComposeTool(
                id = "binary_size_benchmark",
                nameCn = "Payload 大小基准测试",
                nameEn = "Binary Size Benchmark",
                codeTemplate = BINARY_BENCHMARK_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(8, "复杂 UI（100+ 组件）的 Skia 二进制可能达到 500KB+", AnnotationType.WARNING),
                    CodeAnnotation(12, "gzip 压缩率通常在 70-80%，对 Skia 二进制效果显著", AnnotationType.TIP)
                )
            )
        )

        RemoteComposeSection.AB_TESTING -> listOf(
            RemoteComposeTool(
                id = "ab_testing_integration",
                nameCn = "A/B 变体切换集成模板",
                nameEn = "A/B Variant Switching Integration",
                codeTemplate = AB_TESTING_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(9, "variantId 通过 HTTP Header 或 URL 参数传递给服务器", AnnotationType.TIP),
                    CodeAnnotation(15, "实时切换：服务器可随时下发不同 variantId 的 Payload", AnnotationType.IMPORTANT),
                    CodeAnnotation(22, "ABTestMetrics 用于记录变体切换事件，支持 Firebase/自建分析", AnnotationType.TIP)
                )
            ),
            RemoteComposeTool(
                id = "variant_router",
                nameCn = "变体路由示例",
                nameEn = "Variant Router Example",
                codeTemplate = VARIANT_ROUTER_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(6, "变体路由逻辑可在客户端或服务器端实现，推荐服务器端", AnnotationType.TIP),
                    CodeAnnotation(11, "用户分群策略：uid hash 后取模，保证同一用户每次路由结果一致", AnnotationType.IMPORTANT)
                )
            )
        )

        RemoteComposeSection.ACCESSIBILITY -> listOf(
            RemoteComposeTool(
                id = "talkback_compatibility",
                nameCn = "TalkBack 适配指南",
                nameEn = "TalkBack Compatibility Guide",
                codeTemplate = TALKBACK_GUIDE_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(7, "RemoteComposePlayer 内的 Composable 必须随 Payload 下发 semantics 信息", AnnotationType.WARNING),
                    CodeAnnotation(12, "动态内容的 Accessibility 信息无法静态分析，需服务器端协同", AnnotationType.IMPORTANT),
                    CodeAnnotation(18, "Modifier.semantics 必须添加到每个需要 TalkBack 支持的组件", AnnotationType.TIP)
                )
            ),
            RemoteComposeTool(
                id = "dynamic_content_guide",
                nameCn = "动态内容 Accessibility 规范",
                nameEn = "Dynamic Content Accessibility Spec",
                codeTemplate = DYNAMIC_ACCESSIBILITY_TEMPLATE,
                language = CodeLanguage.MARKDOWN,
                annotations = listOf(
                    CodeAnnotation(3, "Payload 中必须包含每个可交互元素的 accessibilityLabel", AnnotationType.IMPORTANT),
                    CodeAnnotation(9, "焦点管理：TalkBack 模式下的焦点遍历顺序需明确定义", AnnotationType.TIP)
                )
            )
        )

        RemoteComposeSection.MONITORING -> listOf(
            RemoteComposeTool(
                id = "render_metrics_collector",
                nameCn = "渲染指标收集器",
                nameEn = "Render Metrics Collector",
                codeTemplate = RENDER_METRICS_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(8, "三个核心指标：渲染成功率 / 渲染延迟 / 崩溃率", AnnotationType.TIP),
                    CodeAnnotation(14, "RenderMetricsCollector 作为单例，跨页面共享", AnnotationType.IMPORTANT),
                    CodeAnnotation(22, "使用快慢通道分离：成功指标走快通道，详情走异步", AnnotationType.SUCCESS)
                )
            ),
            RemoteComposeTool(
                id = "monitoring_dashboard",
                nameCn = "监控仪表盘配置",
                nameEn = "Monitoring Dashboard Config",
                codeTemplate = MONITORING_DASHBOARD_TEMPLATE,
                language = CodeLanguage.YAML,
                annotations = listOf(
                    CodeAnnotation(5, "Prometheus + Grafana 是推荐的开源监控栈", AnnotationType.TIP),
                    CodeAnnotation(11, "SLO 目标：渲染成功率 > 99.5%，P99 渲染延迟 < 2s", AnnotationType.IMPORTANT)
                )
            )
        )

        RemoteComposeSection.FALLBACK_STRATEGY -> listOf(
            RemoteComposeTool(
                id = "fallback_ui_template",
                nameCn = "Fallback UI 模板",
                nameEn = "Fallback UI Template",
                codeTemplate = FALLBACK_UI_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(8, "Fallback UI 使用现有 Material 3 主题，与框架风格一致", AnnotationType.SUCCESS),
                    CodeAnnotation(13, "AlertDialog 展示错误信息，TextButton 提供「重试」操作", AnnotationType.TIP),
                    CodeAnnotation(19, "isLoading 时显示 CircularProgressIndicator，与框架风格一致", AnnotationType.TIP)
                )
            ),
            RemoteComposeTool(
                id = "network_failure_handler",
                nameCn = "网络失败处理器",
                nameEn = "Network Failure Handler",
                codeTemplate = NETWORK_FAILURE_TEMPLATE,
                language = CodeLanguage.KOTLIN,
                annotations = listOf(
                    CodeAnnotation(7, "指数退避重试：首次 1s，第二次 2s，第三次 4s，最多 3 次", AnnotationType.IMPORTANT),
                    CodeAnnotation(12, "缓存优先策略：先展示缓存 Payload，再异步获取新 Payload", AnnotationType.TIP),
                    CodeAnnotation(18, "所有网络错误统一记录，便于服务端排查", AnnotationType.WARNING)
                )
            )
        )

        RemoteComposeSection.MIGRATION_PATH -> listOf(
            RemoteComposeTool(
                id = "migration_steps",
                nameCn = "从 JSON SDUI 迁移步骤",
                nameEn = "JSON SDUI → Remote Compose Migration Steps",
                codeTemplate = MIGRATION_STEPS_TEMPLATE,
                language = CodeLanguage.MARKDOWN,
                annotations = listOf(
                    CodeAnnotation(2, "Phase 1（1-2周）：搭建 Remote Compose 环境，验证最小可行 Payload", AnnotationType.TIP),
                    CodeAnnotation(8, "Phase 2（2-4周）：灰度 5% 流量，对比 JSON SDUI 与 Remote Compose 性能", AnnotationType.IMPORTANT),
                    CodeAnnotation(15, "Phase 4（持续）：建立 Remote Compose 组件库，沉淀最佳实践", AnnotationType.SUCCESS)
                )
            )
        )
    }
}

// ================================================================
// Code Templates — 代码模板（设计文档第 3 节模块结构）
// ================================================================

private const val CLIENT_PLAYER_TEMPLATE = """
@Composable
fun RemoteComposePlayerDemo() {
    // RemoteComposePlayer — 核心渲染组件（框架提供）
    // RemoteComposePlayer: Core rendering component provided by the framework
    var payloadStatus by remember { mutableStateOf(PayloadStatus.IDLE) }
    var renderError by remember { mutableStateOf<RenderError?>(null) }

    // alpha06 版本必须指定，否则可能引入不兼容问题
    // Version alpha06 must be specified to avoid compatibility issues
    val playerVersion = remember { "alpha06" }

    when (payloadStatus) {
        PayloadStatus.DOWNLOADING -> {
            // Loading 使用 Material CircularProgressIndicator，与框架风格一致
            CircularProgressIndicator()
        }
        PayloadStatus.READY -> {
            RemoteComposePlayer(
                payloadUrl = "https://api.example.com/remote-ui/v1/payload",
                version = playerVersion,
                onRenderError = { error ->
                    // onRenderError 回调处理 Skia 渲染失败，必须实现降级逻辑
                    // onRenderError handles Skia rendering failures, fallback logic is mandatory
                    renderError = error
                },
                onVersionMismatch = { serverVersion, clientVersion ->
                    // versionCode 用于版本协商，客户端上报支持的最大版本
                    // versionCode is used for version negotiation
                    payloadStatus = PayloadStatus.DEPRECATED
                }
            )
        }
        PayloadStatus.ERROR, PayloadStatus.DEPRECATED -> {
            // Error 状态用 AlertDialog + TextButton
            ErrorFallback(
                error = renderError,
                onRetry = { payloadStatus = PayloadStatus.DOWNLOADING }
            )
        }
        else -> {}
    }
}
"""

private const val GRADLE_CONFIG_TEMPLATE = """
// settings.gradle.kts 或 build.gradle.kts
// Remote Compose alpha/beta 阶段仅在指定 Maven 仓库可用
// Remote Compose is only available via specific Maven repos during alpha/beta

dependencyResolutionManagement {
    repositories {
        mavenCentral()  // MavenCentral 优先
        google()        // Google Maven 作为备用源
    }
}

dependencies {
    // Remote Compose 核心库（alpha06）
    // alpha06 version is required for compatibility
    implementation("androidx.compose.remotecompose:remotecompose:0.6.0-alpha06")

    // Skia 二进制序列化需要 kotlinx-serialization
    // Skia binary serialization requires kotlinx-serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")

    // 网络请求（推荐使用 Retrofit + OkHttp）
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Payload 下载进度监听
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
"""

private const val EXAMPLE_ACTIVITY_TEMPLATE = """
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    RemoteComposeHost(
                        payloadUrl = "https://api.example.com/remote-ui/v1/payload",
                        clientVersion = "alpha06"
                    )
                }
            }
        }
    }
}

@Composable
fun RemoteComposeHost(
    payloadUrl: String,
    clientVersion: String
) {
    var uiState by remember { mutableStateOf<RemoteUiState> }
    val viewModel = remember { RemoteComposeViewModel() }

    // 观察 ViewModel 状态变化
    LaunchedEffect(payloadUrl) {
        viewModel.processIntent(RemoteUiIntent.LoadPayload(payloadUrl))
    }

    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                // Payload 下载中状态
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            state.renderError != null -> {
                // fallback UI 使用现有 Material 3 主题
                // Fallback UI uses existing Material 3 theme, consistent with framework style
                ErrorFallback(
                    error = state.renderError,
                    onRetry = { viewModel.processIntent(RemoteUiIntent.Retry) }
                )
            }
            else -> {
                // 正常渲染
                RemoteComposePlayer(
                    payloadUrl = payloadUrl,
                    version = clientVersion
                )
            }
        }
    }
}
"""

private const val SERVER_DSL_GUIDE_TEMPLATE = """
// ============================================================
// Remote Compose 服务器端 Composable 编写规范
// Server-Side Composable Writing Specification
// ============================================================
// 核心约束：纯函数
// Core Constraint: Pure Functions
//
// 纯函数约束：Composable 必须是纯函数，无副作用，否则序列化失败
// Pure Function Constraint: Composables must be pure functions without
// side effects, otherwise serialization will fail.

@RemoteComposable  // @RemoteComposable 注解标记可序列化的 Composable 函数
// @RemoteComposable marks serializable Composable functions
data class CardPayload(
    val title: String,
    val description: String,
    val imageUrl: String,
    val actionLabel: String
)

@RemoteComposable
fun RemoteCard(payload: CardPayload): RemoteComposeNode {
    // ✅ 正确：纯函数，无副作用
    // Pure function, no side effects
    return Column {
        Text(payload.title)
        Text(payload.description)
        Button(payload.actionLabel) { /* ... */ }
    }
    // 所有参数必须支持 kotlinx.serialization
    // All parameters must support kotlinx.serialization
}

// ❌ 错误示例：包含副作用的 Composable
@RemoteComposable
fun BadCard(payload: CardPayload): RemoteComposeNode {
    val scope = rememberCoroutineScope()  // ❌ 副作用：启动协程
    scope.launch { doNetworkCall() }       // ❌ 副作用：网络请求
    return Column { Text(payload.title) }
}

// Payload 压缩传输规范
// Payload Compression & Transmission Spec
//
// Skia 二进制默认 gzip 压缩后传输
// Skia binary is gzip-compressed before transmission by default
// - 压缩率：通常 70-80%
// - 传输：HTTP/2 + gzip
// - 缓存：支持 ETag / Last-Modified
"""

private const val PAYLOAD_DESIGN_TEMPLATE = """
// ============================================================
// Payload 结构设计
// Payload Structure Design
// ============================================================
// 版本协商字段
// version 字段用于版本协商，客户端上报支持的最大版本
// version is used for version negotiation, client reports max supported version

data class RemoteUiPayload(
    val version: Int,           // Payload 版本号（从 1 开始递增）
    val schemaVersion: String,   // Schema 版本（如 "1.0.0"）
    val timestamp: Long,         // timestamp 用于缓存控制和版本判断
    val node: SkiaNode,          // Skia 二进制节点（框架序列化）
    val variantId: String = "default",  // A/B 变体 ID
    val compression: CompressionType = CompressionType.GZIP
) {
    /**
     * 检查 Payload 是否过期（超过 24 小时）
     * Check if payload is expired (> 24 hours)
     */
    fun isExpired(): Boolean {
        val maxAgeMs = 24 * 60 * 60 * 1000L  // 24 hours in milliseconds
        return System.currentTimeMillis() - timestamp > maxAgeMs
    }
}

// 压缩类型枚举
enum class CompressionType {
    NONE,   // 不压缩 / No compression
    GZIP    // Gzip 压缩（推荐）/ Gzip compression (recommended)
}

/**
 * 检查 Payload 版本兼容性
 * Check if payload version is compatible with client version
 */
fun isPayloadCompatible(payloadVersion: Int, clientMaxVersion: Int): Boolean {
    // 客户端上报支持的最大版本，服务器下发 <= clientMaxVersion 的 Payload
    // Client reports max supported version, server delivers payload <= clientMaxVersion
    return payloadVersion <= clientMaxVersion
}
"""