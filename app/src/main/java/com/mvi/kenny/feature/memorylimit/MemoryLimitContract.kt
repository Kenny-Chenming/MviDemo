package com.mvi.kenny.feature.memorylimit

/**
 * ============================================================
 * MemoryLimitContract — 内存限制检测 MVI 契约
 * ============================================================
 * PRD-123 | Android 17 App 内存限制检测与 LeakCanary Profiler 集成工具包
 *
 * MVI 三要素：
 * - State: 页面状态的唯一真相来源，Immutable data class
 * - Intent: 用户意图，ViewModel 收到后执行业务逻辑
 * - Effect: 一次性副作用，通过 Channel 传递
 *
 * @see MemoryLimitViewModel
 * @see MemoryLimitScreen
 */

// ================================================================
// Tab index constants / Tab 索引常量
// ================================================================
object MemoryLimitTab {
    const val OVERVIEW = 0
    const val LEAK_CANARY = 1
    const val ANOMALY = 2
    const val AUDIO = 3
    const val CI = 4
}

// ================================================================
// Risk levels / 风险级别
// ================================================================
/**
 * 内存限制风险级别
 *
 * LOW: 内存使用 < 60% 限制，风险低
 * MEDIUM: 内存使用 60-80% 限制，需要关注
 * HIGH: 内存使用 > 80% 限制，高风险
 */
enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

/**
 * 泄漏排序选项
 *
 * BY_RISK: 按触限风险评分排序
 * BY_SIZE: 按泄漏大小排序
 * BY_TIME: 按检测时间排序
 */
enum class LeakSortOption {
    BY_RISK, BY_SIZE, BY_TIME
}

// ================================================================
// Device info / 设备信息
// ================================================================
/**
 * 设备信息
 *
 * @param model 设备型号
 * @param totalRamMb 总 RAM（MB）
 * @param androidVersion Android 版本
 * @param memoryLimitMb 系统内存限制（MB）
 */
data class DeviceInfo(
    val model: String,
    val totalRamMb: Int,
    val androidVersion: Int,
    val memoryLimitMb: Int
)

// ================================================================
// Leak result / 泄漏结果
// ================================================================
/**
 * LeakCanary 检测到的泄漏结果
 *
 * @param leakSignature 泄漏签名（唯一标识）
 * @param leakSizeKb 泄漏大小（KB）
 * @param leakPath 引用链路径
 * @param riskScore 触限风险评分 (0-100)
 * @param detectedAt 检测时间戳
 * @param suggestedFix 建议修复方案
 */
data class LeakResult(
    val leakSignature: String,
    val leakSizeKb: Int,
    val leakPath: String,
    val riskScore: Int,
    val detectedAt: Long,
    val suggestedFix: String
)

// ================================================================
// Anomaly trigger / 异常触发记录
// ================================================================
/**
 * 内存限制异常触发记录
 *
 * @param id 记录 ID
 * @param triggerTime 触发时间
 * @param triggerReason 触发原因
 * @param heapDumpSizeKb 堆转储文件大小（KB）
 * @param heapDumpPath 堆转储文件路径
 */
data class AnomalyTrigger(
    val id: Long,
    val triggerTime: Long,
    val triggerReason: String,
    val heapDumpSizeKb: Int,
    val heapDumpPath: String
)

// ================================================================
// Audio affected path / 受影响音频路径
// ================================================================
/**
 * 受影响的音频代码路径
 *
 * @param apiName API 名称
 * @param filePath 源文件路径
 * @param lineNumber 代码行号
 * @param isAffected 是否受影响
 * @param description 场景描述
 */
data class AudioPath(
    val apiName: String,
    val filePath: String,
    val lineNumber: Int,
    val isAffected: Boolean,
    val description: String
)

// ================================================================
// State / 页面状态
// ================================================================
/**
 * 页面状态
 *
 * @param currentTab 当前 Tab 索引
 * @param currentMemoryUsageMb 当前内存使用量（MB）
 * @param memoryLimitMb 内存限制（MB）
 * @param memoryUsagePercent 内存使用百分比
 * @param isNearLimit 是否接近限制（> 80%）
 * @param riskLevel 风险级别
 * @param selectedDevice 选中设备
 * @param memoryTrendData 最近 5 分钟内存趋势数据（每秒一个点）
 * @param leakCanaryResults LeakCanary 检测结果
 * @param sortOption 泄漏排序选项
 * @param expandedLeakSignature 展开的泄漏项签名
 * @param anomalyTriggers 异常触发记录
 * @param audioAffectedPaths 受影响音频路径
 * @param ciConfigYaml CI 配置 YAML
 * @param generatedScript 生成的脚本
 * @param isAnomalyEnabled 是否启用异常触发
 * @param errorMessage 错误信息
 */
data class MemoryLimitState(
    val currentTab: Int = MemoryLimitTab.OVERVIEW,
    val currentMemoryUsageMb: Float = 0f,
    val memoryLimitMb: Float = 0f,
    val memoryUsagePercent: Float = 0f,
    val isNearLimit: Boolean = false,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val selectedDevice: DeviceInfo? = null,
    val memoryTrendData: List<Float> = emptyList(),
    val leakCanaryResults: List<LeakResult> = emptyList(),
    val sortOption: LeakSortOption = LeakSortOption.BY_RISK,
    val expandedLeakSignature: String? = null,
    val anomalyTriggers: List<AnomalyTrigger> = emptyList(),
    val audioAffectedPaths: List<AudioPath> = emptyList(),
    val ciConfigYaml: String = "",
    val generatedScript: String? = null,
    val isAnomalyEnabled: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = MemoryLimitState()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================
/**
 * 用户意图
 *
 * @see MemoryLimitViewModel.sendIntent
 */
sealed interface MemoryLimitIntent {
    /** 切换 Tab / Select tab */
    data class SelectTab(val index: Int) : MemoryLimitIntent

    /** 选择设备 / Select device */
    data class SelectDevice(val device: DeviceInfo) : MemoryLimitIntent

    /** 刷新内存使用 / Refresh memory usage */
    data object RefreshMemoryUsage : MemoryLimitIntent

    /** 设置泄漏排序 / Set leak sort option */
    data class SetLeakSortOption(val option: LeakSortOption) : MemoryLimitIntent

    /** 展开/折叠泄漏详情 / Toggle leak detail */
    data class ToggleLeakDetail(val signature: String) : MemoryLimitIntent

    /** 启用/禁用异常触发 / Toggle anomaly trigger */
    data class SetAnomalyEnabled(val enabled: Boolean) : MemoryLimitIntent

    /** 生成 CI 配置 / Generate CI config */
    data object GenerateCIConfig : MemoryLimitIntent

    /** 复制脚本 / Copy script */
    data class CopyScript(val script: String) : MemoryLimitIntent

    /** 查看堆转储 / View heap dump */
    data class ViewHeapDump(val triggerId: Long) : MemoryLimitIntent

    /** 分享报告 / Share report */
    data class ShareReport(val content: String) : MemoryLimitIntent
}

// ================================================================
// Effect / 副作用
// ================================================================
/**
 * 副作用
 *
 * @see MemoryLimitViewModel
 */
sealed interface MemoryLimitEffect {
    /** 显示 Snackbar 提示 / Show snackbar */
    data class ShowSnackbar(val message: String) : MemoryLimitEffect

    /** 跳转到源码 / Navigate to source */
    data class NavigateToSource(val filePath: String, val lineNumber: Int) : MemoryLimitEffect

    /** 复制到剪贴板 / Copy to clipboard */
    data class CopyToClipboard(val text: String) : MemoryLimitEffect

    /** 分享报告 / Share report */
    data class ShareReport(val content: String) : MemoryLimitEffect
}
