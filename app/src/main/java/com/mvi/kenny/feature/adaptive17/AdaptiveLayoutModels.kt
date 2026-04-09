package com.mvi.kenny.feature.adaptive17

import androidx.compose.ui.graphics.Color

// =============================================================
// ViolationSeverity — 违规严重程度
// =============================================================
enum class ViolationSeverity(val label: String, val labelZh: String, val color: Color) {
    CRITICAL("CRITICAL", "严重", Color(0xFFD93025)),
    WARNING("WARNING", "中等", Color(0xFFF9AB00)),
    LOW("LOW", "低级", Color(0xFF1E8E3E))
}

// =============================================================
// ViolationType — 违规类型
// =============================================================
enum class ViolationType(
    val label: String,
    val labelZh: String,
    val description: String
) {
    SCREEN_ORIENTATION(
        label = "screenOrientation",
        labelZh = "屏幕方向锁定",
        description = "android:screenOrientation 在 Android 17 大屏设备（sw>600dp）上将被忽略"
    ),
    RESIZEABLE_ACTIVITY(
        label = "resizeableActivity",
        labelZh = "可调整大小限制",
        description = "android:resizeableActivity=false 在 Android 17 大屏上将无效"
    ),
    MAX_ASPECT_RATIO(
        label = "maxAspectRatio",
        labelZh = "最大宽高比限制",
        description = "android:maxAspectRatio 限制在 Android 17 大屏上将不生效"
    ),
    MIN_ASPECT_RATIO(
        label = "minAspectRatio",
        labelZh = "最小宽高比限制",
        description = "android:minAspectRatio 限制在 Android 17 大屏上将不生效"
    ),
    CAMERAX_ASPECT_RATIO(
        label = "CameraXAspectRatio",
        labelZh = "CameraX 预览比例",
        description = "CameraX 预览未使用动态 aspect ratio，方向变化时可能拉伸"
    ),
    CONFIG_CHANGES(
        label = "configChanges",
        labelZh = "配置变更处理",
        description = "configChanges 未包含 screenSize，方向变化时可能重建 Activity"
    )
}

// =============================================================
// LayoutScenario — 布局场景类型
// =============================================================
enum class LayoutScenario(
    val label: String,
    val labelZh: String,
    val description: String
) {
    CAMERA(
        label = "CameraX Preview",
        labelZh = "摄像头预览",
        description = "CameraX 预览场景，支持动态 aspect ratio 切换"
    ),
    VIDEO(
        label = "Video Player",
        labelZh = "视频播放",
        description = "视频播放场景，支持动态缩放和方向适配"
    ),
    FORM(
        label = "Form Input",
        labelZh = "表单输入",
        description = "表单输入场景，支持响应式布局"
    ),
    GENERIC(
        label = "Generic Responsive",
        labelZh = "通用响应式",
        description = "通用响应式布局，基于 WindowSizeClass"
    )
}

// =============================================================
// Violation — 违规项
// =============================================================
data class Violation(
    val id: String,
    val activityName: String,
    val filePath: String,
    val lineNumber: Int,
    val violationType: ViolationType,
    val severity: ViolationSeverity,
    val description: String,
    val suggestedFix: String,
    val isAutoFixable: Boolean = false
)

// =============================================================
// ScanReport — 扫描报告
// =============================================================
data class ScanReport(
    val totalViolations: Int,
    val criticalCount: Int,
    val warningCount: Int,
    val lowCount: Int,
    val violations: List<Violation>,
    val scannedActivities: List<String>,
    val scanTimestamp: Long = System.currentTimeMillis(),
    val reportFilePath: String? = null
)

// =============================================================
// TemplateResult — 模板生成结果
// =============================================================
data class TemplateResult(
    val scenario: LayoutScenario,
    val activityName: String,
    val generatedCode: String,
    val filePath: String
)
