package com.mvi.kenny.feature.qaframework

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 手机端 App QA 扫描器（纯本地，无需 ADB）
 */
object AppScanner {

    private const val TAG = "AppScanner"

    data class ScanReport(
        val packageName: String,
        val appName: String,
        val versionName: String,
        val versionCode: Long,
        val targetSdk: Int,
        val minSdk: Int,
        val isSystemApp: Boolean,
        val isDebuggable: Boolean,
        val isCleartextPermitted: Boolean,
        val apkSizeMb: Long,
        val permissions: List<String>,
        val dangerousPermissions: List<String>,
        val privacyPermissions: List<String>,
        val memoryClass: Int,
        val largeMemoryClass: Int,
        val totalMemoryMb: Long,
        val availMemoryMb: Long,
        val overallScore: Int,
        val issues: List<QAIssue>
    )

    private val DANGEROUS = setOf(
        "android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS",
        "android.permission.READ_SMS", "android.permission.SEND_SMS",
        "android.permission.RECORD_AUDIO", "android.permission.CAMERA",
        "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_PHONE_STATE", "android.permission.CALL_PHONE",
        "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG",
        "android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR",
        "android.permission.BODY_SENSORS", "android.permission.ACCESS_BACKGROUND_LOCATION"
    )

    private val PRIVACY = setOf(
        "android.permission.READ_CONTACTS", "android.permission.READ_CALL_LOG",
        "android.permission.READ_SMS", "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION", "android.permission.READ_PHONE_STATE"
    )

    suspend fun scan(context: Context, packageName: String): ScanReport = withContext(Dispatchers.IO) {
        val pm = context.packageManager

        val pkgInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(
                    (PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA).toLong()
                ))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA)
            }
        } catch (e: Exception) {
            throw Exception("App 未安装: $packageName")
        }

        val appInfo = pkgInfo.applicationInfo ?: throw Exception("无法获取应用信息")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val appName = pm.getApplicationLabel(appInfo).toString()
        val versionName = pkgInfo.versionName ?: "unknown"
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            pkgInfo.versionCode.toLong()
        }

        val targetSdk = appInfo.targetSdkVersion
        val minSdk = appInfo.minSdkVersion
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isDebug = (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val isCleartext = (appInfo.flags and ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC) != 0

        val apkPath = appInfo.sourceDir
        val apkSize = java.io.File(apkPath).length() / (1024 * 1024) // MB

        val perms = pkgInfo.requestedPermissions?.toList() ?: emptyList()
        val dangerous = perms.filter { it in DANGEROUS }
        val privacy = perms.filter { it in PRIVACY }

        val memClass = activityManager.memoryClass
        val largeMemClass = activityManager.largeMemoryClass
        val totalMemMb = memInfo.totalMem / (1024 * 1024)
        val availMemMb = memInfo.availMem / (1024 * 1024)

        val issues = mutableListOf<QAIssue>()

        // Debug 检查
        if (isDebug) {
            issues.add(QAIssue(
                id = "sec_debug", screenIndex = 4, screenshotPath = "",
                description = "应用可调试（DEBUGGABLE）— 发布版本应关闭",
                severity = IssueSeverity.ERROR,
                suggestions = listOf("关闭 DEBUGGABLE 标志", "使用 ProGuard/R8 混淆代码")
            ))
        }

        // 明文流量
        if (isCleartext) {
            issues.add(QAIssue(
                id = "sec_cleartext", screenIndex = 4, screenshotPath = "",
                description = "允许明文 HTTP 流量 — 数据传输不安全",
                severity = IssueSeverity.ERROR,
                suggestions = listOf("强制使用 HTTPS", "配置 network_security_config.xml")
            ))
        }

        // 隐私敏感权限
        privacy.forEach { perm ->
            issues.add(QAIssue(
                id = "priv_$perm", screenIndex = 2, screenshotPath = "",
                description = "隐私敏感权限：$perm",
                severity = IssueSeverity.WARNING,
                suggestions = listOf("确认必要性", "在隐私政策中说明", "提供权限拒绝的降级方案")
            ))
        }

        // 权限过多
        if (dangerous.size > 8) {
            issues.add(QAIssue(
                id = "perm_count", screenIndex = 2, screenshotPath = "",
                description = "危险权限数量过多：${dangerous.size} 个",
                severity = IssueSeverity.WARNING,
                suggestions = listOf("审查每个权限的必要性", "移除非核心权限")
            ))
        }

        // targetSdk 过旧
        if (targetSdk < 33) {
            issues.add(QAIssue(
                id = "comp_target", screenIndex = 5, screenshotPath = "",
                description = "targetSdk=$targetSdk 落后最新版本（34）",
                severity = IssueSeverity.WARNING,
                suggestions = listOf("升级 targetSdk 到 34", "跟进 Android 14 合规要求")
            ))
        }

        // 内存建议
        if (largeMemClass > 0 && largeMemClass < 256) {
            issues.add(QAIssue(
                id = "mem_class", screenIndex = 3, screenshotPath = "",
                description = "内存受限设备（memoryClass=${memClass}MB）— 注意内存优化",
                severity = IssueSeverity.INFO,
                suggestions = listOf("使用 LeakCanary 检测泄漏", "LazyColumn 替代长列表", "图片使用 Coil 并设缓存")
            ))
        }

        // 系统 App 提示
        if (isSystem) {
            issues.add(QAIssue(
                id = "info_system", screenIndex = 1, screenshotPath = "",
                description = "系统预装应用 — 权限需更谨慎",
                severity = IssueSeverity.INFO,
                suggestions = listOf("最小权限原则", "避免申请非必要权限")
            ))
        }

        // 稳定性建议
        issues.add(QAIssue(
            id = "stab_monitor", screenIndex = 6, screenshotPath = "",
            description = "建议集成崩溃监控（Firebase Crashlytics）",
            severity = IssueSeverity.INFO,
            suggestions = listOf("收集崩溃堆栈+设备信息", "建立崩溃聚合和根因分析")
        ))

        var score = 100
        score -= issues.count { it.severity == IssueSeverity.ERROR } * 20
        score -= issues.count { it.severity == IssueSeverity.WARNING } * 10
        score -= issues.count { it.severity == IssueSeverity.INFO } * 3

        ScanReport(
            packageName = packageName,
            appName = appName,
            versionName = versionName,
            versionCode = versionCode,
            targetSdk = targetSdk,
            minSdk = minSdk,
            isSystemApp = isSystem,
            isDebuggable = isDebug,
            isCleartextPermitted = isCleartext,
            apkSizeMb = apkSize,
            permissions = perms,
            dangerousPermissions = dangerous,
            privacyPermissions = privacy,
            memoryClass = memClass,
            largeMemoryClass = largeMemClass,
            totalMemoryMb = totalMemMb,
            availMemoryMb = availMemMb,
            overallScore = score.coerceIn(0, 100),
            issues = issues
        )
    }

    fun getInstalledApps(context: Context): List<Pair<String, String>> {
        val pm = context.packageManager
        val pkgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }
        return pkgs
            .filter { it.applicationInfo != null }
            .map { it.packageName to pm.getApplicationLabel(it.applicationInfo!!).toString() }
            .sortedBy { it.second }
    }
}

