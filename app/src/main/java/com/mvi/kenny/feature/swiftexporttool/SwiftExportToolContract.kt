package com.mvi.kenny.feature.swiftexporttool

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * SwiftExportToolContract — Swift Export iOS 原生互联络工具包 MVI 契约
 * ============================================================
 * PRD-232 | Kotlin 2.2.20 Swift Export iOS 原生互联络工具包
 *
 * 功能模块（5 Tab）：
 * - Tab 1: Swift Export 配置扫描器
 * - Tab 2: SKIE → Swift Export 迁移扫描器
 * - Tab 3: Swift Export iOS 集成模板
 * - Tab 4: CI 校验工具
 * - Tab 5: Swift Concurrency × Swift Export 最佳实践
 *
 * 颜色语义（Terminal/CLI 暗色主题）：
 * - Background: #1A1A2E / #16213E
 * - Primary: #00D9FF (cyan)
 * - Success: #00FF9F (green)
 * - Warning: #FFB300 (amber)
 * - Error: #FF4757 (red)
 * - Text: White #FFFFFF / Gray #A0A0B0
 * - Code block bg: #0D1117
 *
 * @see SwiftExportToolViewModel 状态管理逻辑
 * @see SwiftExportToolScreen UI 渲染层
 */

// ============================================================
// Color Palette / 颜色调色板
// ============================================================

/** Swift Export 工具包专用颜色语义 */
object SwiftExportColors {
    val Background = Color(0xFF1A1A2E)       // 深色背景
    val Surface = Color(0xFF16213E)          // 面板背景
    val Primary = Color(0xFF00D9FF)           // Cyan 主色
    val Success = Color(0xFF00FF9F)          // 绿色成功
    val Warning = Color(0xFFFFB300)          // Amber 警告
    val Error = Color(0xFFFF4757)            // Red 错误
    val TextPrimary = Color(0xFFFFFFFF)      // 白色主文字
    val TextSecondary = Color(0xFFA0A0B0)    // 灰色次要文字
    val CodeBlockBg = Color(0xFF0D1117)      // 代码块背景
    val Border = Color(0xFF30363D)           // 边框
}

// ============================================================
// Enums / 枚举定义
// ============================================================

/** Tab 类型枚举 */
enum class SwiftExportTab(val title: String, val icon: String) {
    SWIFT_EXPORT_SCANNER("Swift Export 配置扫描", "radar"),
    SKIE_MIGRATION("SKIE → Swift Export 迁移", "swap_horiz"),
    IOS_TEMPLATE("iOS 集成模板", "description"),
    CI_VALIDATION("CI 校验工具", "check_circle"),
    CONCURRENCY("Swift Concurrency 最佳实践", "async")
}

/** 扫描状态 */
enum class ScanState { IDLE, SCANNING, COMPLETED, ERROR }

/** 扫描结果类型 */
enum class ScanResultType { SUCCESS, WARNING, ERROR }

/** 迁移决策节点类型 */
enum class DecisionNodeType { CONDITION, ACTION, END }

/** 模板类型 */
enum class TemplateType { PACKAGE_SWIFT, SPM_STEPS, XCODE_PROJECT, GRADLE_CONFIG }

/** CI 检查项状态 */
enum class CICheckState { PENDING, RUNNING, PASSED, FAILED }

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Swift Export 配置扫描结果
 *
 * @param id 唯一 ID
 * @param configName 配置名称
 * @param configValue 配置值
 * @param sourceFile 来源文件
 * @param lineNumber 行号
 * @param resultType 结果类型
 * @param message 消息
 */
data class SwiftExportConfigResult(
    val id: String,
    val configName: String,
    val configValue: String,
    val sourceFile: String,
    val lineNumber: Int,
    val resultType: ScanResultType,
    val message: String = ""
)

/**
 * SKIE 迁移决策树节点
 *
 * @param id 节点 ID
 * @param label 节点标签
 * @param description 描述
 * @param nodeType 节点类型
 * @param children 子节点
 * @param recommendation 推荐操作
 * @param isExpanded 是否展开
 */
data class DecisionTreeNode(
    val id: String,
    val label: String,
    val description: String = "",
    val nodeType: DecisionNodeType,
    val children: List<DecisionTreeNode> = emptyList(),
    val recommendation: String = "",
    val isExpanded: Boolean = false
)

/**
 * SKIE 注解扫描结果
 *
 * @param id 唯一 ID
 * @param annotationName 注解名称
 * @param annotatedElement 被注解元素
 * @param sourceFile 源文件
 * @param lineNumber 行号
 * @param decisionTree 决策树
 * @param isMigratable 是否可迁移
 */
data class SKIEAnnotationResult(
    val id: String,
    val annotationName: String,
    val annotatedElement: String,
    val sourceFile: String,
    val lineNumber: Int,
    val decisionTree: DecisionTreeNode,
    val isMigratable: Boolean = true
)

/**
 * iOS 集成模板内容
 *
 * @param type 模板类型
 * @param name 模板名称
 * @param content 模板内容
 * @param checksum SHA256 校验和
 * @param description 描述
 */
data class IOSTemplate(
    val type: TemplateType,
    val name: String,
    val content: String,
    val checksum: String = "",
    val description: String = ""
)

/**
 * CI 检查项
 *
 * @param id 检查项 ID
 * @param name 检查项名称
 * @param description 描述
 * @param status 当前状态
 * @param message 消息
 * @param durationMs 执行耗时（毫秒）
 */
data class CICheckItem(
    val id: String,
    val name: String,
    val description: String = "",
    val status: CICheckState = CICheckState.PENDING,
    val message: String = "",
    val durationMs: Long = 0
)

/**
 * CI 校验结果
 *
 * @param totalChecks 总检查数
 * @param passedChecks 通过数
 * @param failedChecks 失败数
 * @param items 检查项列表
 * @param overallPassed 是否全部通过
 * @param gradleTaskChain Gradle 任务链
 */
data class CIValidationResult(
    val totalChecks: Int = 0,
    val passedChecks: Int = 0,
    val failedChecks: Int = 0,
    val items: List<CICheckItem> = emptyList(),
    val overallPassed: Boolean = false,
    val gradleTaskChain: String = ""
)

/**
 * Swift Concurrency 最佳实践条目
 *
 * @param id 唯一 ID
 * @param patternName 模式名称
 * @param category 分类
 * @param codeExample 代码示例
 * @param description 描述
 * @param benefits 好处
 * @param isExpanded 是否展开
 */
data class ConcurrencyBestPractice(
    val id: String,
    val patternName: String,
    val category: String,
    val codeExample: String,
    val description: String,
    val benefits: List<String> = emptyList(),
    val isExpanded: Boolean = false
)

// ============================================================
// MVI State / 页面状态
// ============================================================

/**
 * 主状态 — Swift Export 工具包状态容器
 *
 * @param selectedTab 当前选中的 Tab
 * @param scanState 扫描状态
 * @param scanProgress 扫描进度 0-100
 * @param swiftExportConfigs Swift Export 配置扫描结果列表
 * @param skieAnnotationResults SKIE 注解扫描结果列表
 * @param rootDecisionNode 根决策树节点
 * @param templates iOS 集成模板列表
 * @param ciValidationResult CI 校验结果
 * @param bestPractices Swift Concurrency 最佳实践列表
 * @param selectedConfigId 选中的配置 ID
 * @param selectedAnnotationId 选中的注解 ID
 * @param expandedPracticeId 展开的最佳实践 ID
 * @param errorMessage 错误信息
 */
data class SwiftExportToolState(
    val selectedTab: SwiftExportTab = SwiftExportTab.SWIFT_EXPORT_SCANNER,
    val scanState: ScanState = ScanState.IDLE,
    val scanProgress: Int = 0,
    val swiftExportConfigs: List<SwiftExportConfigResult> = emptyList(),
    val skieAnnotationResults: List<SKIEAnnotationResult> = emptyList(),
    val rootDecisionNode: DecisionTreeNode? = null,
    val templates: List<IOSTemplate> = emptyList(),
    val ciValidationResult: CIValidationResult = CIValidationResult(),
    val bestPractices: List<ConcurrencyBestPractice> = emptyList(),
    val selectedConfigId: String? = null,
    val selectedAnnotationId: String? = null,
    val expandedPracticeId: String? = null,
    val errorMessage: String? = null,
) {
    /** 获取选中的配置 */
    val selectedConfig: SwiftExportConfigResult?
        get() = swiftExportConfigs.find { it.id == selectedConfigId }

    /** 获取选中的注解 */
    val selectedAnnotation: SKIEAnnotationResult?
        get() = skieAnnotationResults.find { it.id == selectedAnnotationId }

    /** 是否正在扫描 */
    val isScanning: Boolean get() = scanState == ScanState.SCANNING

    /** CI 检查进度百分比 */
    val ciProgressPercent: Float
        get() {
            if (ciValidationResult.totalChecks == 0) return 0f
            val completed = ciValidationResult.items.count {
                it.status == CICheckState.PASSED || it.status == CICheckState.FAILED
            }
            return completed.toFloat() / ciValidationResult.totalChecks
        }
}

// ============================================================
// MVI Intent / 用户意图
// ============================================================

/**
 * 用户意图 sealed class
 */
sealed class SwiftExportToolIntent {
    // Tab 切换
    data class SelectTab(val tab: SwiftExportTab) : SwiftExportToolIntent()

    // Tab 1: Swift Export 配置扫描
    data object StartSwiftExportScan : SwiftExportToolIntent()
    data object CancelScan : SwiftExportToolIntent()
    data class SelectConfig(val configId: String?) : SwiftExportToolIntent()

    // Tab 2: SKIE → Swift Export 迁移
    data object StartSKIEScan : SwiftExportToolIntent()
    data class ToggleDecisionNode(val nodeId: String) : SwiftExportToolIntent()
    data class SelectAnnotation(val annotationId: String?) : SwiftExportToolIntent()

    // Tab 3: iOS 集成模板
    data class SelectTemplate(val templateType: TemplateType) : SwiftExportToolIntent()
    data class CopyTemplate(val templateType: TemplateType) : SwiftExportToolIntent()

    // Tab 4: CI 校验
    data object RunCIValidation : SwiftExportToolIntent()
    data object CancelCIValidation : SwiftExportToolIntent()

    // Tab 5: Swift Concurrency
    data class TogglePracticeExpanded(val practiceId: String) : SwiftExportToolIntent()
    data class CopyPracticeCode(val practiceId: String) : SwiftExportToolIntent()

    // 全局
    data object DismissError : SwiftExportToolIntent()
}

// ============================================================
// MVI Effect / 一次性副作用
// ============================================================

/**
 * 一次性副作用
 */
sealed class SwiftExportToolEffect {
    data class ShowToast(val message: String) : SwiftExportToolEffect()
    data class CopyToClipboard(val text: String, val label: String) : SwiftExportToolEffect()
    data class ShowError(val title: String, val message: String) : SwiftExportToolEffect()
}
