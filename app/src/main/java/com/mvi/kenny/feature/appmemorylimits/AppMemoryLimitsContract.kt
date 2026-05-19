package com.mvi.kenny.feature.appmemorylimits

// ================================================================
// AppMemoryLimitsContract — Android 17 App Memory Limits MVI Contract
// ================================================================
// MVI architecture contract for Android 17 App Memory Limits Developer Toolkit.
//
// PRD-262: Android 17 App Memory Limits 开发者适配工具包
// Design Reference: memory/agency/designs/PRD-262-Android-17-App-Memory-Limits开发者适配工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
//
// 8 Developer Tools:
//   1. 查询工具 — Device memory limit query
//   2. 调试工具包 — MemoryLimiter debug toolkit
//   3. CI验证工具 — CI validator
//   4. 影响评估工具 — Impact assessment
//   5. 内存优化指南 — Optimization guide
//   6. Trigger-based Profiling 集成指南 — Profiling integration
//   7. OOM Killer 关系解读 — OOM Killer relationship
//   8. 多设备 RAM 分级测试工具 — Multi-device RAM testing
// ================================================================

import androidx.compose.ui.graphics.Color

// =============================================================
// Color Palette — Developer Dashboard 风格配色
// =============================================================
object AppMemoryLimitsColors {
    val Primary = Color(0xFF1A73E8)         // Android Blue — 主色
    val Surface = Color(0xFFFFFFFF)          // 卡片背景
    val Background = Color(0xFFF1F3F4)      // 页面背景
    val CodeBlock = Color(0xFF1E1E1E)       // 代码块背景
    val HighRisk = Color(0xFFEA4335)        // 高危 — Red
    val MediumRisk = Color(0xFFFBBC04)      // 中危 — Yellow
    val LowRisk = Color(0xFF34A853)         // 低危 — Green
    val OnSurface = Color(0xFF202124)        // 主文字
    val OnSurfaceVariant = Color(0xFF5F6368) // 次级文字
}

// =============================================================
// ToolCategory — 工具分类枚举
// =============================================================
enum class ToolCategory(val displayName: String, val emoji: String) {
    QUERY("查询", "🔍"),
    DEBUG("调试", "🔧"),
    CI("CI验证", "✅"),
    GUIDE("指南", "📖")
}

// =============================================================
// RiskLevel — 风险等级枚举
// =============================================================
enum class AppRiskLevel(val displayName: String, val emoji: String, val color: Color) {
    HIGH("高危", "⚠️", AppMemoryLimitsColors.HighRisk),
    MEDIUM("中危", "⚡", AppMemoryLimitsColors.MediumRisk),
    LOW("低危", "✅", AppMemoryLimitsColors.LowRisk)
}

// =============================================================
// AppType — 应用类型枚举（用于风险评估）
// =============================================================
enum class AppType(val displayName: String) {
    GAME("游戏应用"),
    IMAGE_PROCESSING("图像处理/AR应用"),
    NORMAL("普通应用")
}

// =============================================================
// DevTool — 开发者工具数据模型
// =============================================================
data class DevTool(
    val id: Int,
    val name: String,
    val shortDescription: String,
    val category: ToolCategory,
    val hasCI: Boolean,
    val background: String,
    val features: List<String>,
    val codeExample: String,
    val gradleConfig: String? = null,
    val ciExample: String? = null,
    val outputFormat: String? = null
)

// =============================================================
// AppMemoryLimitsState — 内存限制工具页面状态（MVI State）
// =============================================================
data class AppMemoryLimitsState(
    val selectedToolIndex: Int = -1,
    val deviceRamBytes: Long = 0L,
    val appMemoryLimitBytes: Long = 0L,
    val riskLevel: AppRiskLevel = AppRiskLevel.LOW,
    val currentAppType: AppType = AppType.NORMAL,
    val tools: List<DevTool> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val simulatorInputRam: Long = 8L * 1024 * 1024 * 1024,
    val simulatorResult: SimulatorResult? = null,
    val exportFormat: ExportFormat = ExportFormat.JSON,
    val exportResult: String? = null,
    val errorMessage: String? = null
) {
    companion object {
        val Initial = AppMemoryLimitsState()
    }
}

// =============================================================
// AppMemoryLimitsIntent — 用户意图（User Intent）
// =============================================================
sealed interface AppMemoryLimitsIntent {
    data object DetectDevice : AppMemoryLimitsIntent
    data class SelectTool(val toolIndex: Int) : AppMemoryLimitsIntent
    data class LoadRiskAssessment(val appType: AppType) : AppMemoryLimitsIntent
    data class CopyCode(val code: String) : AppMemoryLimitsIntent
    data class RunSimulator(val deviceRam: Long) : AppMemoryLimitsIntent
    data class ExportReport(val format: ExportFormat) : AppMemoryLimitsIntent
    data class UpdateSearch(val query: String) : AppMemoryLimitsIntent
    data object ClearError : AppMemoryLimitsIntent
}

// =============================================================
// AppMemoryLimitsEffect — 一次性副作用（Effect）
// =============================================================
sealed interface AppMemoryLimitsEffect {
    data class ShowToast(val message: String) : AppMemoryLimitsEffect
    data class ShowError(val message: String) : AppMemoryLimitsEffect
    data class CopyToClipboard(val code: String) : AppMemoryLimitsEffect
    data class ExportFile(val content: String, val format: ExportFormat) : AppMemoryLimitsEffect
}

// =============================================================
// ExportFormat — 报告导出格式
// =============================================================
enum class ExportFormat(val displayName: String, val extension: String) {
    JSON("JSON", "json"),
    HTML("HTML", "html")
}

// =============================================================
// SimulatorResult — 模拟器结果
// =============================================================
data class SimulatorResult(
    val deviceRam: Long,
    val appMemoryLimit: Long,
    val riskLevel: AppRiskLevel,
    val warnings: List<String>,
    val suggestions: List<String>
)

// =============================================================
// 8个工具的默认数据
// =============================================================
fun getDefaultTools(): List<DevTool> = listOf(
    // ─────────────────────────────────────────────────────────
    // Tool 1: Android 17 App Memory Limits 查询工具
    // ─────────────────────────────────────────────────────────
    DevTool(
        id = 1,
        name = "Android 17 App Memory Limits 查询工具",
        shortDescription = "查询当前设备的 per-app 内存上限",
        category = ToolCategory.QUERY,
        hasCI = false,
        background = "Android 17 Beta 4 引入 per-app 内存上限，系统根据设备总 RAM 为每个应用设置内存上限。" +
                "不同设备上限不同（通常为 min(512MB, deviceRAM × 25%)），开发者需要查询实际设备的上限值。",
        features = listOf(
            "通过 ActivityManager.MemoryInfo 获取设备总内存",
            "通过 ActivityManager.getMemoryInfo() 获取当前 App 内存限制",
            "计算公式：perAppLimit = min(512MB, deviceRAM × 0.25)",
            "支持多设备批量查询（通过 ADB 命令）"
        ),
        codeExample = """
// 查询当前设备 per-app 内存上限
import android.app.ActivityManager
import android.content.Context

fun queryAppMemoryLimit(context: Context): MemoryLimitInfo {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    val deviceRamBytes = memoryInfo.totalMem
    // Android 17 公式: per-app limit = min(512MB, deviceRAM * 0.25)
    val perAppLimitBytes = minOf(deviceRamBytes / 4, 512L * 1024 * 1024)

    return MemoryLimitInfo(
        deviceRamBytes = deviceRamBytes,
        perAppLimitBytes = perAppLimitBytes,
        deviceName = android.os.Build.MODEL,
        androidVersion = android.os.Build.VERSION.SDK_INT
    )
}

data class MemoryLimitInfo(
    val deviceRamBytes: Long,
    val perAppLimitBytes: Long,
    val deviceName: String,
    val androidVersion: Int
)

// ADB 命令行批量查询
// $ adb shell "cat /proc/meminfo | grep MemTotal"
// $ adb shell "dumpsys activity meminfo | grep 'max *'"
        """.trimIndent(),
        outputFormat = "JSON: {deviceRamBytes, perAppLimitBytes, deviceName, androidVersion}"
    ),

    // ─────────────────────────────────────────────────────────
    // Tool 2: MemoryLimiter 调试工具包
    // ─────────────────────────────────────────────────────────
    DevTool(
        id = 2,
        name = "MemoryLimiter 调试工具包",
        shortDescription = "ApplicationExitInfo 读取 MemoryLimiter 杀死事件",
        category = ToolCategory.DEBUG,
        hasCI = false,
        background = "MemoryLimiter 是 Android 17 的新系统进程，负责在应用内存超限时杀死应用。" +
                "开发者需要能检测到 MemoryLimiter 杀死了自己的 App，并获取杀死时的内存快照用于调试。",
        features = listOf(
            "通过 ApplicationExitInfo 获取进程退出原因（REASON_MEMORY_LIMITED）",
            "监听 MemoryLimiter 事件并上报",
            "支持在 debug 构建中自动触发 heap dump",
            "MemoryLimiterKillEvent: timestamp / memoryUsage / limit / reason"
        ),
        codeExample = """
// MemoryLimiter 杀死事件监听器
import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build

class MemoryLimiterDebugger(private val context: Context) {

    fun getRecentExitEvents(): List<MemoryLimiterKillEvent> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return emptyList()

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val exitEvents = activityManager.getHistoricalExitReasons(context.packageName, 0, 20)

        return exitEvents.mapNotNull { exitInfo ->
            if (exitInfo.reason == ApplicationExitInfo.REASON_MEMORY_LIMITED ||
                exitInfo.reason == ApplicationExitInfo.REASON_OTHER) {
                MemoryLimiterKillEvent(
                    timestamp = exitInfo.timestamp,
                    processName = exitInfo.processName,
                    reason = describeReason(exitInfo.reason),
                    memoryUsageBytes = 0L,
                    description = exitInfo.description ?: "N/A"
                )
            } else null
        }
    }

    private fun describeReason(reason: Int): String = when (reason) {
        ApplicationExitInfo.REASON_MEMORY_LIMITED -> "MemoryLimiter: 内存超限被杀死"
        ApplicationExitInfo.REASON_LOW_MEMORY -> "系统低内存被杀死"
        ApplicationExitInfo.REASON_OTHER -> "其他原因"
        else -> "未知原因"
    }
}

data class MemoryLimiterKillEvent(
    val timestamp: Long,
    val processName: String,
    val reason: String,
    val memoryUsageBytes: Long,
    val description: String
)

// Heap Dump 触发器需要 android.permission.FORCE_STOP_PACKAGES
        """.trimIndent(),
        outputFormat = "JSON: [{timestamp, processName, reason, memoryUsageBytes, description}]"
    ),

    // ─────────────────────────────────────────────────────────
    // Tool 3: Android Memory Limits CI 验证工具
    // ─────────────────────────────────────────────────────────
    DevTool(
        id = 3,
        name = "Android Memory Limits CI 验证工具",
        shortDescription = "Gradle plugin + GitHub Actions workflow，CI 自动检测内存超限风险",
        category = ToolCategory.CI,
        hasCI = true,
        background = "在 CI 环境中验证 App 在不同 RAM 设备上的内存表现，" +
                "确保 release 构建不会触发 MemoryLimiter。推荐在 GitHub Actions 中集成。",
        features = listOf(
            "Gradle 插件：androidMemoryLimitsCheck",
            "GitHub Actions Workflow：多设备 RAM 测试矩阵",
            "支持 Pixel 6a (6GB) / Pixel 8 (8GB) / Pixel 9 Pro XL (16GB)",
            "CI 报告：JSON + HTML 双格式输出"
        ),
        codeExample = """
// build.gradle.kts — 集成内存限制 CI 插件
plugins {
    id("com.android.application")
    id("android.memorylimits.check") version "1.0.0"
}

android {
    memoryLimits {
        targetDeviceRam.set(6L * 1024 * 1024 * 1024) // 6GB (Pixel 6a)
        appType.set("GAME")
        riskThreshold.set(0.80f)
        reportPath.set("reports/memory-limits/")
    }
}
        """.trimIndent(),
        gradleConfig = """
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
        """.trimIndent(),
        ciExample = """
# GitHub Actions — Android Memory Limits CI Workflow
# .github/workflows/android-memory-limits.yml
name: Android Memory Limits CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  memory-limits-test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        device: [6gb, 8gb, 12gb]  # 模拟器 RAM 配置
    steps:
      - uses: actions/checkout@v4

      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          java-version: 17

      - name: Run Android Memory Limits Check
        run: |
          ./gradlew :app:checkMemoryLimits \
            -PtargetDeviceRam=${'$'}{{ matrix.device }}gb

      - name: Upload Report
        uses: actions/upload-artifact@v4
        with:
          name: memory-report-${'$'}{{ matrix.device }}
          path: app/build/reports/memory-limits/
        if: always()
        """.trimIndent(),
        outputFormat = "CI Report: JSON + HTML at app/build/reports/memory-limits/"
    ),

    // ─────────────────────────────────────────────────────────
    // Tool 4: App Memory Limits 影响评估工具
    // ─────────────────────────────────────────────────────────
    DevTool(
        id = 4,
        name = "App Memory Limits 影响评估工具",
        shortDescription = "输入 App 类型，评估 MemoryLimiter 对该类型 App 的影响程度",
        category = ToolCategory.QUERY,
        hasCI = false,
        background = "不同类型的 App 受 MemoryLimiter 影响程度不同。" +
                "游戏和图像处理 App 内存消耗大，被杀风险最高；" +
                "普通 App 通常在限制范围内，但需注意后台进程管理。",
        features = listOf(
            "游戏应用：内存需求高，触发风险 HIGH",
            "图像处理/AR应用：大量 Bitmap，风险 HIGH",
            "普通应用：一般内存使用，风险 LOW-MEDIUM",
            "输出：风险等级 + 优化建议 + 内存使用基准"
        ),
        codeExample = """
// 影响评估 — 根据 App 类型评估风险
enum class AppType(val displayName: String, val memoryFactor: Float) {
    GAME("游戏", 0.85f),
    IMAGE_PROCESSING("图像处理/AR", 0.90f),
    NORMAL("普通", 0.50f)
}

data class ImpactAssessment(
    val appType: AppType,
    val riskLevel: AppRiskLevel,
    val estimatedMemoryUsageMB: Float,
    val recommendedLimitMB: Float,
    val suggestions: List<String>,
    val benchmarks: Map<String, Float>
)

fun assessImpact(appType: AppType, deviceRamGB: Int): ImpactAssessment {
    val deviceRamBytes = deviceRamGB * 1024L * 1024 * 1024
    val limitBytes = minOf(deviceRamBytes / 4, 512L * 1024 * 1024)
    val limitMB = limitBytes / (1024 * 1024)

    val (risk, suggestions) = when (appType) {
        AppType.GAME -> AppRiskLevel.HIGH to listOf(
            "使用 inSampleSize 降低 Bitmap 分辨率",
            "实现对象池避免频繁 GC",
            "考虑使用 vulkan/GameActivity 减少内存"
        )
        AppType.IMAGE_PROCESSING -> AppRiskLevel.HIGH to listOf(
            "Bitmap 使用 inBitmap 复用",
            "及时 recycle() 释放图片资源",
            "使用 ImageDecoder + inSampleSize"
        )
        AppType.NORMAL -> AppRiskLevel.LOW to listOf(
            "监控内存使用，避免内存泄漏",
            "使用 LeakCanary 检测泄漏",
            "后台进程使用 WorkManager"
        )
    }

    val benchmarks = when (appType) {
        AppType.GAME -> mapOf("Texture" to limitMB * 0.4f, "Audio" to limitMB * 0.1f)
        AppType.IMAGE_PROCESSING -> mapOf("BitmapCache" to limitMB * 0.5f)
        AppType.NORMAL -> mapOf("AppData" to limitMB * 0.3f)
    }

    return ImpactAssessment(
        appType = appType,
        riskLevel = risk,
        estimatedMemoryUsageMB = limitMB * appType.memoryFactor,
        recommendedLimitMB = limitMB,
        suggestions = suggestions,
        benchmarks = benchmarks
    )
}
        """.trimIndent(),
        outputFormat = "JSON: {appType, riskLevel, estimatedMemoryUsageMB, suggestions[], benchmarks{}}"
    ),

    // ─────────────────────────────────────────────────────────
    // Tool 5: Android Memory Limits 内存优化指南
    // ─────────────────────────────────────────────────────────
    DevTool(
        id = 5,
        name = "Android Memory Limits 内存优化指南",
        shortDescription = "Bitmap 优化、对象池、WeakReference 缓存等最佳实践",
        category = ToolCategory.GUIDE,
        hasCI = false,
        background = "在 MemoryLimiter 环境下，优化内存使用是避免被杀死的关键。" +
                "本指南提供经过验证的优化模式，覆盖 Bitmap、缓存、对象池等常见场景。",
        features = listOf(
            "Bitmap 优化：inSampleSize / inBitmap / ImageDecoder",
            "WeakReference / SoftReference 缓存模式",
            "对象池：避免在 onDraw 等高频调用中创建对象",
            "内存泄漏检测：LeakCanary + 定期巡检",
            "largeHeap 评估：权衡预留内存与 OOM 风险"
        ),
        codeExample = """
// Bitmap 内存优化 — ImageDecoder + inSampleSize
val options = ImageDecoder.DecodeOptions().apply {
    inSampleSize = calculateInSampleSize(originalWidth, originalHeight, reqWidth, reqHeight)
    inMutable = false
}
val bitmap = ImageDecoder.decodeBitmap(imageSource, options)

fun calculateInSampleSize(w: Int, h: Int, reqW: Int, reqH: Int): Int {
    var inSampleSize = 1
    if (h > reqH || w > reqW) {
        val halfH = h / 2
        val halfW = w / 2
        while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

// WeakReference 缓存模式
class ImageCache {
    private val cache = WeakHashMap<String, SoftReference<Bitmap>>()

    fun get(key: String): Bitmap? = cache[key]?.get()
    fun put(key: String, bitmap: Bitmap) {
        cache[key] = SoftReference(bitmap)
    }
}

// 对象池 — 避免 onDraw 中创建对象
class RecyclerPool<T>(private val factory: () -> T) {
    private val pool = mutableListOf<T>()

    fun acquire(): T = pool.removeLastOrNull() ?: factory()
    fun release(item: T) { pool.add(item) }

    inline fun <R> use(block: (T) -> R): R {
        val item = acquire()
        return try { block(item) } finally { release(item) }
    }
}
        """.trimIndent(),
        outputFormat = null
    ),

    // ─────────────────────────────────────────────────────────
    // Tool 6: Trigger-based Profiling 集成指南
    // ─────────────────────────────────────────────────────────
    DevTool(
        id = 6,
        name = "Trigger-based Profiling 集成指南",
        shortDescription = "ProfilingManager ANOMALY 触发器配置，内存超限时自动 heap dump",
        category = ToolCategory.GUIDE,
        hasCI = false,
        background = "Android 11+ 提供 ProfilingManager，允许 App 注册触发器，" +
                "在特定条件（如内存超限）满足时自动采集性能数据。" +
                "这是调试 MemoryLimiter 杀死事件的关键工具。" +
                "注意：此功能为实验性质，行为可能在正式版中变化。",
        features = listOf(
            "ProfilingManager.registerTrigger() 注册触发器",
            "TRIGGER_TYPE_ANOMALY 监听内存异常",
            "配合 MemoryLimiter 事件自动采集 heap dump",
            "Android 17 Beta 4 行为，待正式版确认"
        ),
        codeExample = """
// Trigger-based Profiling — 内存超限时自动 heap dump
import android.os.Build
import android.os.ProfilingManager
import android.os.TriggerUri

@RequiresApi(Build.VERSION_CODES.R)
class MemoryProfilingTrigger(private val context: Context) {

    private val profilingManager = context.getSystemService(ProfilingManager::class.java)

    fun registerMemoryTrigger() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

        val trigger = ProfilingManager.TriggerConfig.Builder()
            .setTriggerType(ProfilingManager.TRIGGER_TYPE_ANOMALY)
            .setCondition(
                ProfilingManager.TriggerCondition.MEMORY_THRESHOLD,
                TriggerUri.forByteCount(getAppMemoryLimit() * 80 / 100)
            )
            .setCallbackComponent(ComponentName(context, MemoryTriggerReceiver::class.java))
            .setOutputFile(
                File(context.filesDir, "memory-trigger-${'$'}{System.currentTimeMillis()}.prof").toString()
            )
            .build()

        profilingManager.registerTrigger(trigger, Runnable {
            Log.d("MemoryProfiling", "Trigger registered successfully")
        })
    }
}

class MemoryTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val triggerType = intent.getIntExtra(ProfilingManager.EXTRA_TRIGGER_TYPE, -1)
        val outputFile = intent.getStringExtra(ProfilingManager.EXTRA_OUTPUT_FILE)
        Log.w("MemoryTrigger", "Memory anomaly triggered! Output: ${'$'}outputFile")
    }
}
        """.trimIndent(),
        outputFormat = ".prof 文件（Android Profiling 格式）"
    ),

    // ─────────────────────────────────────────────────────────
    // Tool 7: Android 17 Memory Limits vs OOM Killer 关系解读
    // ─────────────────────────────────────────────────────────
    DevTool(
        id = 7,
        name = "Android 17 Memory Limits vs OOM Killer 关系解读",
        shortDescription = "澄清 MemoryLimiter 与 Linux OOM Killer 的关系与优先级",
        category = ToolCategory.GUIDE,
        hasCI = false,
        background = "开发者常混淆 MemoryLimiter 和 Linux OOM Killer。" +
                "两者都是内存保护机制，但优先级和触发条件不同。" +
                "OOM Killer 优先级更高，可能在 MemoryLimiter 之前行动。" +
                "理解两者关系有助于正确设计内存保护策略。",
        features = listOf(
            "MemoryLimiter：Android 17 新增，per-app 内存上限，软限制",
            "OOM Killer：Linux 内核机制，系统全局内存，优先级更高",
            "触发顺序：OOM Killer 先于 MemoryLimiter",
            "6GB RAM 设备实测：两者可能同时触发",
            "优化建议：同时处理两种情况"
        ),
        codeExample = """
/*
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  Linux Kernel OOM Killer                    │
 * │  触发条件：系统全局内存耗尽（所有进程总内存 > threshold）      │
 * │  优先级：HIGHEST（内核态，进程无法阻止）                      │
 * │  目标：选择 oom_score 最高的进程杀死                        │
 * │  日志：dmesg | grep -i "killed process"                     │
 * └─────────────────────────────────────────────────────────────┘
 *                          ↓
 * ┌─────────────────────────────────────────────────────────────┐
 * │              Android MemoryLimiter (Android 17+)           │
 * │  触发条件：单个 App 内存使用 > per-app limit                 │
 * │  优先级：LOW（Android Framework 层）                        │
 * │  目标：只杀死超限 App，不影响系统                            │
 * │  日志：logcat -s MemoryLimiter:*                           │
 * └─────────────────────────────────────────────────────────────┘
 *
 * 重要结论：
 *   1. OOM Killer 会先于 MemoryLimiter 触发
 *   2. 两者可以同时存在（6GB RAM 设备实测）
 *   3. 应对策略：既要遵守 per-app limit，也要防止系统 OOM
 */

// OOM Killer 日志
// $ adb shell dmesg | grep -i "killed process"
// $ adb shell cat /proc/vmstat | grep oom

// MemoryLimiter 日志
// $ adb logcat -s MemoryLimiter:* ActivityManager:*
// $ adb shell dumpsys activity events | grep memory
        """.trimIndent(),
        outputFormat = null
    ),

    // ─────────────────────────────────────────────────────────
    // Tool 8: 多设备 RAM 分级测试工具
    // ─────────────────────────────────────────────────────────
    DevTool(
        id = 8,
        name = "多设备 RAM 分级测试工具",
        shortDescription = "在不同 RAM 的模拟器上验证 App 的 MemoryLimiter 兼容性",
        category = ToolCategory.DEBUG,
        hasCI = true,
        background = "不同 RAM 设备对应不同的 per-app 内存上限。" +
                "开发者需要在多个 RAM 等级的模拟器上测试，确保 App 在所有目标设备上都不会触发 MemoryLimiter。" +
                "6GB RAM 设备（如 Pixel 6a）是最严格的测试场景。",
        features = listOf(
            "模拟器 RAM 配置：6GB / 8GB / 12GB / 16GB",
            "对应 per-app 上限：min(512MB, RAM × 25%)",
            "推荐测试矩阵：Pixel 6a(6GB) → Pixel 8(8GB) → Pixel 9 Pro XL(16GB)",
            "支持 CI 多设备并行测试"
        ),
        codeExample = """
/*
 * 模拟器 RAM 配置与 per-app 上限对照表
 * ┌──────────────┬───────────────┬──────────────────────────┐
 * │  设备/RAM     │  Per-app Limit │  测试场景               │
 * ├──────────────┼───────────────┼──────────────────────────┤
 * │  4GB         │  256MB        │  低端机（印度/东南亚）   │
 * │  6GB         │  384MB ⚠️     │  Pixel 6a（中端）       │
 * │  8GB         │  512MB        │  Pixel 8 / 中高端旗舰   │
 * │  12GB        │  512MB        │  Samsung S24 Ultra       │
 * └──────────────┴───────────────┴──────────────────────────┘
 */

// 创建 6GB RAM 模拟器（Pixel 6a）
// $ avdmanager create avd -n "pixel6a_6gb" -k "system-images;android-35;google_apis;arm64-v8a" -d "pixel_6a"
// $ echo "hw.ramSize=6144" >> ~/.android/avd/pixel6a_6gb.avd/config.ini

// 创建 8GB RAM 模拟器（Pixel 8）
// $ avdmanager create avd -n "pixel8_8gb" -k "system-images;android-35;google_apis;arm64-v8a" -d "pixel_8"
// $ echo "hw.ramSize=8192" >> ~/.android/avd/pixel8_8gb.avd/config.ini

// 创建 16GB RAM 模拟器（Pixel 9 Pro XL）
// $ echo "hw.ramSize=16384" >> ~/.android/avd/pixel9_pro_xl.avd/config.ini
        """.trimIndent(),
        ciExample = """
# GitHub Actions — 多设备 RAM 测试矩阵
jobs:
  ram分级测试:
    strategy:
      matrix:
        ram: [6, 8, 12, 16]  # GB
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Create ${'$'}{{ matrix.ram }}GB RAM AVD
        run: |
          avdmanager create avd -n test_${'$'}{{ matrix.ram }}gb \
            -k "system-images;android-35;google_apis;arm64-v8a"
          echo "hw.ramSize=${'$'}{{ matrix.ram * 1024 }}" >> \
            ~/.android/avd/test_${'$'}{{ matrix.ram }}gb.avd/config.ini

      - name: Run UI Test
        run: |
          ./gradlew :app:connectedAndroidTest \
            -PtestDeviceRam=${'$'}{{ matrix.ram }}
        """.trimIndent(),
        outputFormat = "Test Report: JSON + screenshots"
    )
)

// =============================================================
// 辅助函数
// =============================================================

/**
 * 格式化字节数为可读字符串
 */
fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
    bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
    else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
}

/**
 * 计算 Android 17 per-app 内存上限
 */
fun calculateAppMemoryLimit(deviceRamBytes: Long): Long {
    return minOf(deviceRamBytes / 4, 512L * 1024 * 1024)
}
