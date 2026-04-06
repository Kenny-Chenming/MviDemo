package com.mvi.crashanalytics

import java.io.Serializable

/**
 * Crash report data model
 */
data class CrashReport(
    val threadName: String,
    val exceptionType: String,
    val message: String,
    val stackTrace: List<StackFrame>,
    val deviceInfo: DeviceInfo,
    val appVersion: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customMessage: String? = null
) : Serializable {

    data class StackFrame(
        val className: String,
        val methodName: String,
        val fileName: String?,
        val lineNumber: Int
    ) : Serializable

    data class DeviceInfo(
        val manufacturer: String,
        val model: String,
        val osVersion: String,
        val sdkVersion: Int,
        val memoryAvailable: Long,
        val isRooted: Boolean
    ) : Serializable

    fun toFormattedString(): String {
        return buildString {
            appendLine("🔥 Crash Report")
            appendLine("Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(timestamp))}")
            appendLine("App: $appVersion")
            appendLine("Device: ${deviceInfo.manufacturer} ${deviceInfo.model} (Android ${deviceInfo.sdkVersion})")
            appendLine("Exception: $exceptionType: $message")
            appendLine("Thread: $threadName")
            appendLine("--- Stack Trace ---")
            stackTrace.take(15).forEach { frame ->
                appendLine("  at ${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})")
            }
            if (stackTrace.size > 15) {
                appendLine("  ... and ${stackTrace.size - 15} more frames")
            }
        }
    }
}
