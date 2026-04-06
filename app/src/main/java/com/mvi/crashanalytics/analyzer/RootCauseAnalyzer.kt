package com.mvi.crashanalytics.analyzer

import com.mvi.crashanalytics.CrashReport

/**
 * Root cause categorization for Android crashes
 */
enum class CrashCategory(val displayName: String) {
    NULL_POINTER("空指针异常"),
    ILLEGAL_STATE("非法状态异常"),
    MEMORY_ISSUE("内存问题"),
    THREAD_ISSUE("线程安全问题"),
    PERMISSION("权限问题"),
    NETWORK("网络异常"),
    UI_ISSUE("UI线程操作异常"),
    LIBRARY("第三方库问题"),
    UNKNOWN("未知原因")
}

data class RootCauseAnalysis(
    val category: CrashCategory,
    val probability: Float, // 0.0 - 1.0
    val description: String,
    val likelyFrame: CrashReport.StackFrame?,
    val suggestions: List<String>
)

/**
 * AI-powered root cause analyzer for crash reports
 */
object RootCauseAnalyzer {

    private val PATTERN_MAP = mapOf(
        "NullPointerException" to CrashCategory.NULL_POINTER,
        "IllegalStateException" to CrashCategory.ILLEGAL_STATE,
        "IllegalArgumentException" to CrashCategory.ILLEGAL_STATE,
        "OutOfMemoryError" to CrashCategory.MEMORY_ISSUE,
        "StackOverflowError" to CrashCategory.MEMORY_ISSUE,
        "RuntimeException" to CrashCategory.UNKNOWN,
        "SecurityException" to CrashCategory.PERMISSION,
        "NetworkOnMainThreadException" to CrashCategory.THREAD_ISSUE,
        "ViewRootImpl$CalledFromWrongThreadException" to CrashCategory.UI_ISSUE,
        "ActivityNotFoundException" to CrashCategory.UI_ISSUE,
        "FragmentNotFoundException" to CrashCategory.UI_ISSUE,
        "CancellationException" to CrashCategory.THREAD_ISSUE,
        "JSONException" to CrashCategory.ILLEGAL_STATE,
        "ParseException" to CrashCategory.ILLEGAL_STATE
    )

    private val LIBRARY_PACKAGES = setOf(
        "com.google.android",
        "androidx.",
        "kotlinx.coroutines",
        "okhttp3",
        "retrofit2",
        "com.squareup",
        "io.reactivex",
        "dagger.hilt",
        "org.jetbrains.kotlin"
    )

    private val SUGGESTION_MAP = mapOf(
        CrashCategory.NULL_POINTER to listOf(
            "在调用方法前添加 null 检查",
            "使用 Kotlin 的安全调用操作符 (?.) 替代直接调用",
            "检查对象初始化时机，确保在使用前已赋值",
            "考虑使用 Elvis 操作符 (?:) 提供默认值"
        ),
        CrashCategory.ILLEGAL_STATE to listOf(
            "检查状态转换是否合法",
            "确保在正确的生命周期阶段调用对应方法",
            "添加状态校验，提前抛出明确的异常信息",
            "使用 sealed class 管理合法状态流转"
        ),
        CrashCategory.MEMORY_ISSUE to listOf(
            "检查是否存在内存泄漏，使用 LeakCanary 定位",
            "优化大对象存储，避免在主线程加载大量数据",
            "检查 Bitmap 等大资源是否正确释放",
            "考虑使用 weakReference 存储可回收对象"
        ),
        CrashCategory.THREAD_ISSUE to listOf(
            "将耗时操作移至后台线程（Dispatchers.IO）",
            "避免在主线程进行网络请求",
            "使用 view.post {} 确保在主线程操作 UI",
            "注意协程的取消时机，避免在已取消的协程中更新 UI"
        ),
        CrashCategory.PERMISSION to listOf(
            "在操作前检查并申请所需权限",
            "使用 ActivityResultLauncher 处理权限申请",
            "实现合理的权限拒绝降级逻辑",
            "注意 Android 13+ 的细粒度媒体权限变化"
        ),
        CrashCategory.NETWORK to listOf(
            "添加网络状态检查",
            "实现重试机制，设置合理的超时时间",
            "处理无网络时的降级体验",
            "注意 SSL 证书校验问题"
        ),
        CrashCategory.UI_ISSUE to listOf(
            "确保 UI 操作在主线程执行",
            "检查 Fragment/Activity 生命周期状态",
            "使用 LiveData/StateFlow 确保 UI 更新安全",
            "避免在 onDestroy 后继续操作视图"
        ),
        CrashCategory.LIBRARY to listOf(
            "检查库版本，查看是否有已知的 crash fix",
            "查看库的 issue tracker 确认是否为已知问题",
            "考虑降级到稳定版本或等待修复",
            "如果影响严重，评估替换方案"
        ),
        CrashCategory.UNKNOWN to listOf(
            "收集更多上下文日志",
            "在可疑位置添加 try-catch 定位",
            "使用断点或日志定位具体触发条件",
            "尝试复现路径，逐步缩小范围"
        )
    )

    /**
     * Analyze crash report and return root cause analysis
     */
    fun analyze(report: CrashReport): RootCauseAnalysis {
        val exceptionType = report.exceptionType
        val category = PATTERN_MAP[exceptionType] ?: inferCategory(report)
        val suggestions = SUGGESTION_MAP[category] ?: SUGGESTION_MAP[CrashCategory.UNKNOWN]!!
        val likelyFrame = findLikelyRootCause(report.stackTrace)

        val description = generateDescription(category, report, likelyFrame)
        val probability = calculateProbability(category, report)

        return RootCauseAnalysis(
            category = category,
            probability = probability,
            description = description,
            likelyFrame = likelyFrame,
            suggestions = suggestions.shuffled().take(3)
        )
    }

    private fun inferCategory(report: CrashReport): CrashCategory {
        val message = report.message.lowercase()
        val firstFrame = report.stackTrace.firstOrNull()

        return when {
            message.contains("null") -> CrashCategory.NULL_POINTER
            message.contains("memory") || message.contains("heap") -> CrashCategory.MEMORY_ISSUE
            message.contains("permission") || message.contains("grant") -> CrashCategory.PERMISSION
            message.contains("network") || message.contains("http") -> CrashCategory.NETWORK
            message.contains("main thread") -> CrashCategory.THREAD_ISSUE
            firstFrame?.className?.startsWith("androidx.") == true -> CrashCategory.UI_ISSUE
            else -> CrashCategory.UNKNOWN
        }
    }

    private fun findLikelyRootCause(frames: List<CrashReport.StackFrame>): CrashReport.StackFrame? {
        // Skip library frames, find first app code frame
        return frames.firstOrNull { frame ->
            !LIBRARY_PACKAGES.any { frame.className.startsWith(it) }
        } ?: frames.firstOrNull()
    }

    private fun generateDescription(
        category: CrashCategory,
        report: CrashReport,
        likelyFrame: CrashReport.StackFrame?
    ): String {
        val frameInfo = likelyFrame?.let {
            " at ${it.className.substringAfterLast('.')}.${it.methodName}()"
        } ?: ""

        return when (category) {
            CrashCategory.NULL_POINTER ->
                "检测到空指针异常${frameInfo}。通常是对象未初始化或已被回收后仍被访问。"
            CrashCategory.ILLEGAL_STATE ->
                "非法状态异常${frameInfo}。对象处于不合法状态时收到了非法方法调用。"
            CrashCategory.MEMORY_ISSUE ->
                "内存相关异常${frameInfo}。可能是内存泄漏或大对象分配导致 OOM。"
            CrashCategory.THREAD_ISSUE ->
                "线程安全问题${frameInfo}。检测到在非预期线程执行了操作。"
            CrashCategory.PERMISSION ->
                "权限相关异常${frameInfo}。操作前未检查或申请必要权限。"
            CrashCategory.NETWORK ->
                "网络相关异常${frameInfo}。可能是网络不可用、超时或协议错误。"
            CrashCategory.UI_ISSUE ->
                "UI相关异常${frameInfo}。在错误的线程或生命周期状态下操作了视图。"
            CrashCategory.LIBRARY ->
                "第三方库异常${frameInfo}。问题来源于第三方依赖而非 app 代码。"
            CrashCategory.UNKNOWN ->
                "未能在自动分析中确定明确原因${frameInfo}，建议手动分析堆栈。"
        }
    }

    private fun calculateProbability(category: CrashCategory, report: CrashReport): Float {
        var probability = 0.7f

        // High confidence if we have a direct pattern match
        if (report.exceptionType in PATTERN_MAP.keys) {
            probability = 0.85f
        }

        // Lower confidence for UNKNOWN category
        if (category == CrashCategory.UNKNOWN) {
            probability = 0.5f
        }

        // Lower confidence if no app frames found (likely library issue)
        val hasAppFrame = report.stackTrace.any { frame ->
            !LIBRARY_PACKAGES.any { frame.className.startsWith(it) }
        }
        if (!hasAppFrame) {
            probability = 0.6f
        }

        return probability
    }
}
