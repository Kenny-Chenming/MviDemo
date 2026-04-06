package com.mvi.crashanalytics

import android.util.Log
import com.mvi.crashanalytics.analyzer.FixSuggestionGenerator
import com.mvi.crashanalytics.analyzer.RootCauseAnalysis
import com.mvi.crashanalytics.analyzer.RootCauseAnalyzer
import com.mvi.crashanalytics.parser.StackTraceParser

/**
 * Main crash analytics engine
 * Usage:
 *   val engine = CrashAnalyticsEngine()
 *   engine.processCrash(rawLog, deviceInfo, appVersion) { analysis ->
 *       println("Root cause: ${analysis.category}")
 *   }
 */
class CrashAnalyticsEngine(
    private val enableAutoFix: Boolean = true,
    private val minConfidence: Float = 0.5f
) {
    private val parser = StackTraceParser
    private val analyzer = RootCauseAnalyzer
    private val fixGenerator = FixSuggestionGenerator

    interface CrashListener {
        fun onCrashAnalyzed(report: CrashReport, analysis: RootCauseAnalysis)
        fun onFixGenerated(fix: FixSuggestionGenerator.CodeFix)
    }

    private var listener: CrashListener? = null

    fun setListener(listener: CrashListener) {
        this.listener = listener
    }

    /**
     * Process raw crash log and invoke callback with analysis
     */
    fun processCrash(
        rawLog: String,
        deviceInfo: CrashReport.DeviceInfo,
        appVersion: String,
        onComplete: (RootCauseAnalysis, CrashReport) -> Unit
    ) {
        // Step 1: Parse stack trace
        val report = parser.parse(rawLog, deviceInfo, appVersion)
        Log.d(TAG, "Parsed crash: ${report.exceptionType} - ${report.message}")

        // Step 2: Analyze root cause
        val analysis = analyzer.analyze(report)
        Log.d(TAG, "Analysis: category=${analysis.category}, prob=${analysis.probability}")

        // Step 3: Notify listener
        listener?.onCrashAnalyzed(report, analysis)

        // Step 4: Generate fix suggestions if enabled
        if (enableAutoFix && analysis.probability >= minConfidence) {
            val fixes = fixGenerator.generateFix(report, analysis)
            fixes.forEach { fix ->
                Log.d(TAG, "Fix: ${fix.type} - ${fix.explanation}")
                listener?.onFixGenerated(fix)
            }
        }

        onComplete(analysis, report)
    }

    /**
     * Quick analysis without full processing
     */
    fun quickAnalyze(rawLog: String): Pair<String, String> {
        return parser.quickExtract(rawLog)
    }

    /**
     * Generate a full crash report in markdown format
     */
    fun generateMarkdownReport(report: CrashReport, analysis: RootCauseAnalysis): String {
        return buildString {
            appendLine("# 🔥 Crash Analysis Report")
            appendLine()
            appendLine("## 📋 Basic Info")
            appendLine("| Field | Value |")
            appendLine("|-------|-------|")
            appendLine("| Exception | `${report.exceptionType}` |")
            appendLine("| Message | ${report.message} |")
            appendLine("| Thread | `${report.threadName}` |")
            appendLine("| App Version | ${report.appVersion} |")
            appendLine("| Device | ${report.deviceInfo.manufacturer} ${report.deviceInfo.model} |")
            appendLine("| Android | ${report.deviceInfo.sdkVersion} |")
            appendLine("| Root Probability | ${(analysis.probability * 100).toInt()}% |")
            appendLine()
            appendLine("## 🔍 Root Cause Analysis")
            appendLine("**Category:** ${analysis.category.displayName}")
            appendLine()
            appendLine(analysis.description)
            appendLine()
            appendLine("## 📍 Suspected Location")
            analysis.likelyFrame?.let { frame ->
                appendLine("```")
                appendLine("at ${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})")
                appendLine("```")
            }
            appendLine()
            appendLine("## 💡 Suggestions")
            analysis.suggestions.forEachIndexed { index, suggestion ->
                appendLine("${index + 1}. $suggestion")
            }
            appendLine()
            appendLine("## 📐 Stack Trace")
            appendLine("```")
            report.stackTrace.take(20).forEach { frame ->
                appendLine("  at ${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})")
            }
            appendLine("```")
        }
    }

    companion object {
        private const val TAG = "CrashAnalytics"
    }
}
