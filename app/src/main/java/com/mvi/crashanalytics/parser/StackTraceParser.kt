package com.mvi.crashanalytics.parser

import com.mvi.crashanalytics.CrashReport
import com.mvi.crashanalytics.CrashReport.StackFrame

/**
 * Parser for Android/Java crash stack traces
 */
object StackTraceParser {

    private val CAUSE_PATTERN = Regex("Caused by:\\s*(.+?):\\s*(.+)")
    private val EXCEPTION_HEADER_PATTERN = Regex("^(\\S+)\\s*(?:exception|error)?:\\s*(.*)$", RegexOption.IGNORE_CASE)
    private val STACK_FRAME_PATTERN = Regex("^\s+at\\s+(\\S+)\\.(\\S+)\\((\\S+?)(?::(\\d+))?\\)")
    private val THREAD_PATTERN = Regex("^\"([^\"]+)\"\\s+(?:prio=\\d+\\s+)?(?:daemon\\s+)?tid=\\d+\\s+java\\.lang\\.Thread\\s+\\{[^}]+\\}")

    /**
     * Parse a raw crash log string into structured CrashReport
     */
    fun parse(rawLog: String, deviceInfo: CrashReport.DeviceInfo, appVersion: String): CrashReport {
        val lines = rawLog.lines()
        var exceptionType = "UnknownException"
        var message = ""
        val stackFrames = mutableListOf<StackFrame>()
        var threadName = "main"
        var customMessage: String? = null

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Match thread name
            THREAD_PATTERN.find(line)?.let {
                threadName = it.groupValues[1]
            }

            // Match exception header
            EXCEPTION_HEADER_PATTERN.find(line)?.let { match ->
                if (exceptionType == "UnknownException") {
                    exceptionType = match.groupValues[1].substringAfterLast('.')
                    message = match.groupValues[2].trim()
                }
            }

            // Check for "Caused by" chain
            CAUSE_PATTERN.find(line)?.let { match ->
                exceptionType = match.groupValues[1].substringAfterLast('.')
                message = match.groupValues[2].trim()
            }

            // Match stack frame
            STACK_FRAME_PATTERN.find(line)?.let { match ->
                val className = match.groupValues[1]
                val methodName = match.groupValues[2]
                val fileName = match.groupValues[3].takeIf { it != "Native Method" }
                val lineNumber = match.groupValues[4].toIntOrNull() ?: -1
                stackFrames.add(StackFrame(className, methodName, fileName, lineNumber))
            }

            // Collect custom context message (lines before exception)
            if (line.isNotBlank() && !line.startsWith(" ") && exceptionType == "UnknownException") {
                customMessage = line.take(200)
            }

            i++
        }

        return CrashReport(
            threadName = threadName,
            exceptionType = exceptionType,
            message = message,
            stackTrace = stackFrames,
            deviceInfo = deviceInfo,
            appVersion = appVersion,
            customMessage = customMessage
        )
    }

    /**
     * Quick parse: extract exception type and message from logcat output
     */
    fun quickExtract(rawLog: String): Pair<String, String> {
        val lines = rawLog.lines()
        var exceptionType = "Unknown"
        var message = ""

        for (line in lines) {
            EXCEPTION_HEADER_PATTERN.find(line)?.let { match ->
                exceptionType = match.groupValues[1].substringAfterLast('.')
                message = match.groupValues[2].trim()
                return@quickExtract Pair(exceptionType, message)
            }
            CAUSE_PATTERN.find(line)?.let { match ->
                exceptionType = match.groupValues[1].substringAfterLast('.')
                message = match.groupValues[2].trim()
                return@quickExtract Pair(exceptionType, message)
            }
        }
        return Pair(exceptionType, message)
    }
}
