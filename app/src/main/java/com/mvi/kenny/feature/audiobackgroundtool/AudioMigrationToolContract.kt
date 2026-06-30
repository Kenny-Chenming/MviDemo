// ================================================================
// AudioMigrationToolContract — 音频迁移工具 MVI 契约
// Audio Migration Tool MVI Contract
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// MVI 三要素：
// - State：页面状态的唯一真相来源
// - Intent：用户意图（用户操作）
// - Effect：一次性副作用（导航、Toast）
// ================================================================

package com.mvi.kenny.feature.audiobackgroundtool

// ==================== State ====================

/**
 * 音频迁移工具页面状态
 * Audio Migration Tool Page State
 *
 * @param isScanning 是否正在扫描
 * @param isGenerating 是否正在生成脚手架
 * @param isVerifying 是否正在验证
 * @param scanProgress 扫描进度（0-1）
 * @param violations 违规列表
 * @param summary 审计摘要
 * @param selectedViolationIds 选中的违规 ID
 * @param generatedFiles 生成的文件列表
 * @param testScore 兼容性测试分数
 * @param testPassed 测试是否通过
 * @param errorMessage 错误信息
 * @param activeTab 当前活动标签页
 */
data class AudioMigrationToolState(
    val isScanning: Boolean = false,
    val isGenerating: Boolean = false,
    val isVerifying: Boolean = false,
    val scanProgress: Float = 0f,
    val violations: List<AudioViolationUi> = emptyList(),
    val summary: AuditSummaryUi? = null,
    val selectedViolationIds: Set<String> = emptySet(),
    val generatedFiles: List<String> = emptyList(),
    val testScore: Int = 0,
    val testPassed: Boolean = false,
    val errorMessage: String? = null,
    val activeTab: Int = 0  // 0=扫描, 1=生成, 2=验证
) {
    companion object {
        /** 初始状态 */
        val Initial = AudioMigrationToolState()
    }
}

/**
 * 违规 UI 数据
 * Violation UI Data
 *
 * @param id 违规 ID
 * @param severity 严重等级（CRITICAL/WARNING/INFO）
 * @param apiName API 名称
 * @param filePath 文件路径
 * @param line 行号
 * @param suggestedFix 建议修复方案
 * @param isSelected 是否被选中
 */
data class AudioViolationUi(
    val id: String,
    val severity: String,
    val apiName: String,
    val filePath: String,
    val line: Int,
    val suggestedFix: String,
    val isSelected: Boolean = false
)

/**
 * 审计摘要 UI 数据
 * Audit Summary UI Data
 *
 * @param total 总违规数
 * @param critical 严重违规数
 * @param warning 警告违规数
 * @param info 提示违规数
 * @param affectedModules 受影响模块
 */
data class AuditSummaryUi(
    val total: Int,
    val critical: Int,
    val warning: Int,
    val info: Int,
    val affectedModules: List<String>
)

// ==================== Intent ====================

/**
 * 音频迁移工具用户意图
 * Audio Migration Tool User Intent
 */
sealed interface AudioMigrationToolIntent {
    /** 开始扫描 */
    data object StartScan : AudioMigrationToolIntent

    /** 按严重等级筛选
     * @param severity 严重等级（空表示全部）
     */
    data class FilterBySeverity(val severity: String?) : AudioMigrationToolIntent

    /** 切换违规选中状态
     * @param violationId 违规 ID
     */
    data class ToggleViolationSelection(val violationId: String) : AudioMigrationToolIntent

    /** 全选所有违规 */
    data object SelectAllViolations : AudioMigrationToolIntent

    /** 取消全选 */
    data object DeselectAllViolations : AudioMigrationToolIntent

    /** 生成选中的脚手架
     * @param violationIds 要生成脚手架的违规 ID
     */
    data class GenerateScaffolding(val violationIds: List<String>) : AudioMigrationToolIntent

    /** 运行兼容性测试 */
    data object RunCompatibilityTest : AudioMigrationToolIntent

    /** 切换标签页
     * @param tabIndex 标签索引
     */
    data class SwitchTab(val tabIndex: Int) : AudioMigrationToolIntent

    /** 清除错误消息 */
    data object ClearError : AudioMigrationToolIntent

    /** 加载历史报告 */
    data object LoadPreviousReport : AudioMigrationToolIntent
}

// ==================== Effect ====================

/**
 * 音频迁移工具副作用
 * Audio Migration Tool Effect
 *
 * 一次性事件，不可变，只能被消费一次。
 */
sealed interface AudioMigrationToolEffect {
    /** 显示 Toast
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : AudioMigrationToolEffect

    /** 显示错误
     * @param message 错误描述
     */
    data class ShowError(val message: String) : AudioMigrationToolEffect

    /** 显示成功
     * @param message 成功消息
     */
    data class ShowSuccess(val message: String) : AudioMigrationToolEffect

    /** 脚手架生成完成
     * @param files 生成的文件列表
     */
    data class ScaffoldGenerated(val files: List<String>) : AudioMigrationToolEffect

    /** 打开报告文件
     * @param filePath 报告文件路径
     */
    data class OpenReport(val filePath: String) : AudioMigrationToolEffect

    /** 兼容性测试完成
     * @param passed 是否通过
     * @param score 测试分数
     */
    data class TestCompleted(val passed: Boolean, val score: Int) : AudioMigrationToolEffect
}
