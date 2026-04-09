package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * AAPMonitorModels.kt — 数据模型定义
 * AAPMonitorModels.kt — Data Model Definitions
 * ============================================================
 */

// region ScanStatus & AapmStatus

/**
 * 扫描状态枚举
 * Scan status enumeration
 */
enum class ScanStatus {
    IDLE,       // 空闲状态，未开始扫描
    SCANNING,   // 正在扫描中
    DONE,       // 扫描完成
    ERROR       // 扫描出错
}

/**
 * AAPM 激活状态
 * Advanced Protection Mode status
 */
enum class AapmStatus {
    ACTIVE,     // AAPM 已激活
    INACTIVE,   // AAPM 未激活
    UNKNOWN     // 状态未知（无权限获取）
}

// endregion

// region Severity

/**
 * 问题严重程度
 * Issue severity level
 */
enum class Severity {
    P0, // 崩溃级 — 严重影响功能，必须立即修复
    P1, // 功能失效级 — 核心功能不可用
    P2  // 警告级 — 潜在问题，建议修复
}

// endregion

// region FilterOptions

/**
 * 扫描结果筛选选项
 * Filter options for scan results
 */
data class FilterOptions(
    val module: String? = null,     // 按模块筛选
    val apiType: String? = null,    // 按 API 类型筛选
    val filePath: String? = null    // 按文件路径筛选
)

// endregion

// region AccessibilityIssue

/**
 * AccessibilityService 问题数据类
 * AccessibilityService issue data class
 *
 * @property id 唯一标识符
 * @property serviceName 服务名称
 * @property filePath 文件路径
 * @property severity 严重程度 P0/P1/P2
 * @property description 问题描述
 * @property suggestedFix 修复建议
 * @property callChain 调用链（用于火焰图展示）
 * @property isResolved 是否已标记为已解决
 */
data class AccessibilityIssue(
    val id: String,
    val serviceName: String,
    val filePath: String,
    val severity: Severity,
    val description: String,
    val suggestedFix: String,
    val callChain: List<String> = emptyList(), // 调用链，用于火焰图
    val isResolved: Boolean = false
)

// endregion

// region ScopedApiEntry

/**
 * Scoped API 替代方案条目（知识库）
 * Scoped API alternative entry (Knowledge Base)
 *
 * @property name API 名称
 * @property description 功能描述
 * @property alternative 替代方案说明
 * @property applicableScenario 适用场景
 */
data class ScopedApiEntry(
    val name: String,
    val description: String,
    val alternative: String,
    val applicableScenario: String
)

// endregion

// region CallChainEntry

/**
 * 调用链条目（火焰图用）
 * Call chain entry for flame chart visualization
 */
data class CallChainEntry(
    val methodName: String,
    val depth: Int,
    val isExpanded: Boolean = false
)

// endregion

// region ComplianceStep

/**
 * 合规引导步骤定义
 * Compliance guide step definition
 *
 * @property step 步骤序号（1-4）
 * @property title 步骤标题
 * @property description 步骤描述
 * @property isCompleted 是否已完成
 */
data class ComplianceStep(
    val step: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)

// endregion

// region AAPM Device Info

/**
 * 设备 AAPM 信息
 * Device AAPM information
 *
 * @property sdkVersion Android SDK 版本
 * @property isRooted 是否已 Root
 * @property manufacturer 设备制造商
 * @property model 设备型号
 */
data class DeviceAapmInfo(
    val sdkVersion: Int,
    val isRooted: Boolean,
    val manufacturer: String,
    val model: String
)

// endregion

// region Report

/**
 * 扫描报告数据
 * Scan report data
 *
 * @property generatedAt 生成时间
 * @property totalIssues 问题总数
 * @property p0Count P0 问题数
 * @property p1Count P1 问题数
 * @property p2Count P2 问题数
 * @property issues 问题详情列表
 */
data class ScanReport(
    val generatedAt: Long,
    val totalIssues: Int,
    val p0Count: Int,
    val p1Count: Int,
    val p2Count: Int,
    val issues: List<AccessibilityIssue>
)

// endregion
