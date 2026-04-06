package com.mvi.crashanalytics.reporter

import android.content.Context
import android.os.Build
import com.mvi.crashanalytics.CrashAnalyticsEngine
import com.mvi.crashanalytics.CrashReport
import com.mvi.crashanalytics.analyzer.RootCauseAnalysis
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Global crash reporter that can be integrated into Application
 *
 * Usage in Application class:
 *   CrashReporter.install(this) { report, analysis ->
 *       // Send to your crash backend
 *       api.reportCrash(report, analysis)
 *   }
 */
object CrashReporter {

    private var isInstalled = false
    private var callback: ((CrashReport, RootCauseAnalysis) -> Unit)? = null
    private val engine = CrashAnalyticsEngine()

    fun install(
        context: Context,
        onCrashAnalyzed: (report: CrashReport, analysis: RootCauseAnalysis) -> Unit
    ) {
        if (isInstalled) {
            return
        }
        isInstalled = true
        callback = onCrashAnalyzed

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val rawLog = captureStackTrace(throwable)
                val deviceInfo = buildDeviceInfo(context)
                val appVersion = getAppVersion(context)

                engine.processCrash(rawLog, deviceInfo, appVersion) { analysis, report ->
                    callback?.invoke(report, analysis)
                }
            } catch (e: Exception) {
                // Fail silently, let default handler deal with it
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    /**
     * Manually report a caught exception
     */
    fun report(throwable: Throwable, context: Context) {
        try {
            val rawLog = captureStackTrace(throwable)
            val deviceInfo = buildDeviceInfo(context)
            val appVersion = getAppVersion(context)

            engine.processCrash(rawLog, deviceInfo, appVersion) { analysis, report ->
                callback?.invoke(report, analysis)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Generate markdown report for display/export
     */
    fun generateMarkdownReport(throwable: Throwable, context: Context): String {
        val rawLog = captureStackTrace(throwable)
        val deviceInfo = buildDeviceInfo(context)
        val appVersion = getAppVersion(context)
        val report = CrashReport(
            threadName = Thread.currentThread().name,
            exceptionType = throwable.javaClass.simpleName,
            message = throwable.message ?: "No message",
            stackTrace = emptyList(),
            deviceInfo = deviceInfo,
            appVersion = appVersion
        )
        val analysis = com.mvi.crashanalytics.analyzer.RootCauseAnalyzer.analyze(report)
        return CrashAnalyticsEngine().generateMarkdownReport(report, analysis)
    }

    private fun captureStackTrace(throwable: Throwable): String {
        val writer = StringWriter()
        throwable.printStackTrace(PrintWriter(writer))
        return writer.toString()
    }

    private fun buildDeviceInfo(context: Context): CrashReport.DeviceInfo {
        val runtime = Runtime.getRuntime()
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        activityManager.getMemoryInfo(memoryInfo)

        return CrashReport.DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            osVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            memoryAvailable = memoryInfo.availMem,
            isRooted = checkIsRooted()
        )
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun checkIsRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return paths.any { java.io.File(it).exists() }
    }
}
