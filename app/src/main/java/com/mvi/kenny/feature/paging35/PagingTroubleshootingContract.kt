package com.mvi.kenny.feature.paging35

/**
 * ============================================================
 * PagingTroubleshootingContract.kt — 踩坑排查工具 MVI 契约
 * ============================================================
 * PRD-093 | Paging 3.5 `asState` 操作符开发工具包
 *
 * 功能说明：
 * - 问题场景选择（5 种典型场景）
 * - 问题诊断卡片（症状/原因/建议）
 * - 自动化检测开关
 *
 * @author 开心果 🥜
 */

// ============================================================
// TroubleScenario — 问题场景枚举
// ============================================================

/**
 * Paging 3 典型踩坑场景枚举
 *
 * @param title 场景标题
 * @param iconName 图标名称
 */
enum class TroubleScenario(
    val title: String,
    val iconName: String
) {
    DUPLICATE_LOAD("重复加载", "content_copy"),
    STATE_LOSS("状态丢失", "sync_problem"),
    BOUNDARY_ERROR("边界条件", "boundary"),
    ROOM_INTEGRATION("Room 协同问题", "storage"),
    PULL_TO_REFRESH("PullToRefresh 协同", "refresh")
}

// ============================================================
// TroubleCase — 问题案例数据
// ============================================================

/**
 * 问题案例数据
 *
 * @param title 问题标题
 * @param symptom 症状描述
 * @param causes 可能原因列表
 * @param solutions 解决建议列表
 */
data class TroubleCase(
    val title: String,
    val symptom: String,
    val causes: List<String>,
    val solutions: List<SolutionItem>
)

// ============================================================
// MVI State / 状态
// ============================================================

/**
 * 踩坑排查工具完整状态
 *
 * @param selectedScenario 当前选中的场景
 * @param autoDetectEnabled 是否开启自动化检测
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度
 * @param detectedIssues 检测到的问题列表
 * @param expandedCaseIndex 展开的案例索引（-1 表示全部收起）
 *
 * @author 开心果 🥜
 */
data class PagingTroubleshootingState(
    val selectedScenario: TroubleScenario = TroubleScenario.DUPLICATE_LOAD,
    val autoDetectEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val detectedIssues: List<DetectedIssue> = emptyList(),
    val expandedCaseIndex: Int = -1
)

/**
 * 检测到的问题
 *
 * @param filePath 文件路径
 * @param lineNumber 行号
 * @param description 问题描述
 * @param severity 严重程度
 */
data class DetectedIssue(
    val filePath: String,
    val lineNumber: Int,
    val description: String,
    val severity: SeverityLevel
)

// ============================================================
// MVI Intent / 用户意图
// ============================================================

/**
 * 踩坑排查工具用户意图
 *
 * @author 开心果 🥜
 */
sealed class PagingTroubleshootingIntent {

    /** 选择问题场景 */
    data class SelectScenario(val scenario: TroubleScenario) : PagingTroubleshootingIntent()

    /** 切换自动化检测开关 */
    data class ToggleAutoDetect(val enabled: Boolean) : PagingTroubleshootingIntent()

    /** 展开/收起案例 */
    data class ToggleCaseExpand(val index: Int) : PagingTroubleshootingIntent()

    /** 开始扫描 */
    data object StartScan : PagingTroubleshootingIntent()

    /** 停止扫描 */
    data object StopScan : PagingTroubleshootingIntent()
}

// ============================================================
// MVI Effect / 副作用
// ============================================================

/**
 * 踩坑排查工具一次性副作用
 *
 * @author 开心果 🥜
 */
sealed class PagingTroubleshootingEffect {

    /** 显示 Snackbar */
    data class ShowSnackbar(val message: String) : PagingTroubleshootingEffect()

    /** 扫描完成 */
    data class ScanComplete(val issuesFound: Int) : PagingTroubleshootingEffect()
}
