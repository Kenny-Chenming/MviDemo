// ================================================================
// BackgroundAudioDetector — 后台音频检测器
// Background Audio Detector based on AST Analysis
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 使用正则表达式 + 静态分析检测后台音频 API 调用。
// Uses regex-based static analysis to detect background audio API calls.
//
// 检测范围：
// - 简单正则匹配：覆盖大部分场景
// - 完整 AST 分析：使用 kotlin-compile-testing（可选）
// ================================================================

package com.mvi.kenny.audiobackground.engine

import java.io.File

/**
 * 单个音频违规记录
 * Single audio violation record
 *
 * @param id 唯一标识符（UUID）
 * @param severity 严重等级
 * @param apiName API 名称（如 MediaPlayer.start）
 * @param file 违规文件路径
 * @param line 行号
 * @param column 列号
 * @param callStack 调用栈链
 * @param suggestedFix 建议修复方案
 * @param matchedPattern 匹配到的模式
 */
data class AudioViolation(
    val id: String,
    val severity: AudioApiSeverity,
    val apiName: String,
    val file: String,
    val line: Int,
    val column: Int = 0,
    val callStack: List<String> = emptyList(),
    val suggestedFix: String,
    val matchedPattern: AudioApiPattern
) {
    companion object {
        private var counter = 0
        fun generateId(): String = "VIO-${++counter}"
    }
}

/**
 * 音频审计摘要
 * Audio Audit Summary
 *
 * @param totalViolations 总违规数
 * @param criticalCount 严重违规数
 * @param warningCount 警告违规数
 * @param infoCount 提示违规数
 * @param affectedModules 受影响模块列表
 * @param timestamp 扫描时间戳
 */
data class AuditSummary(
    val totalViolations: Int,
    val criticalCount: Int,
    val warningCount: Int,
    val infoCount: Int,
    val affectedModules: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 报告增量（两次扫描对比）
 * Report Delta (comparison between two scans)
 *
 * @param newViolations 新增违规
 * @param resolvedViolations 已修复违规
 * @param unchangedViolations 未变化违规
 */
data class ReportDelta(
    val newViolations: List<AudioViolation>,
    val resolvedViolations: List<AudioViolation>,
    val unchangedViolations: List<AudioViolation>
)

/**
 * 后台音频检测器
 * Background Audio Detector
 *
 * 使用静态分析检测 Kotlin/Java 源码中的后台音频 API 调用。
 * Detects background audio API calls in Kotlin/Java source code using static analysis.
 *
 * @param targetApiLevel 目标 API 等级（默认 37 = Android 13）
 * @param excludePatterns 排除文件模式
 * @param logger 日志输出
 */
class BackgroundAudioDetector(
    private val targetApiLevel: Int = 37,
    private val excludePatterns: List<String> = listOf("**/build/**", "**/.gradle/**"),
    private val logger: (String) -> Unit = { println(it) }
) {
    private val patternMatcher = AudioApiPatternMatcher
    private val callGraphBuilder = CallGraphBuilder()
    private val violationClassifier = ViolationClassifier()

    /**
     * 扫描源码目录中的后台音频违规
     * Scan source directory for background audio violations
     *
     * @param sourceDirs 源码目录列表
     * @return 扫描到的违规列表
     */
    fun scan(sourceDirs: List<String>): List<AudioViolation> {
        val allViolations = mutableListOf<AudioViolation>()

        for (sourceDir in sourceDirs) {
            val dir = File(sourceDir)
            if (!dir.exists()) {
                logger("⚠️  Source directory not found: $sourceDir")
                continue
            }

            logger("📂 Scanning: $sourceDir")

            dir.walkTopDown()
                .filter { it.isFile }
                .filter { it.extension in listOf("kt", "java") }
                .filter { file -> isNotExcluded(file) }
                .forEach { file ->
                    val violations = scanFile(file)
                    allViolations.addAll(violations)
                }
        }

        // 分类并排序
        // Classify and sort violations
        val sorted = violationClassifier.classifyAndSort(allViolations)
        logger("\n✅ Scan complete: ${sorted.size} violations found")
        logger("   🔴 CRITICAL: ${sorted.count { it.severity == AudioApiSeverity.CRITICAL }}")
        logger("   🟡 WARNING:  ${sorted.count { it.severity == AudioApiSeverity.WARNING }}")
        logger("   🔵 INFO:     ${sorted.count { it.severity == AudioApiSeverity.INFO }}")

        return sorted
    }

    /**
     * 扫描单个文件
     * Scan a single file
     */
    private fun scanFile(file: File): List<AudioViolation> {
        val violations = mutableListOf<AudioViolation>()
        val content = try {
            file.readText()
        } catch (e: Exception) {
            logger("⚠️  Failed to read file: ${file.path} - ${e.message}")
            return emptyList()
        }

        val lines = content.lines()

        // 遍历每一行，匹配所有音频 API 模式
        // Iterate each line, match all audio API patterns
        lines.forEachIndexed { index, line ->
            val lineNumber = index + 1

            for (pattern in patternMatcher.allPatterns) {
                if (matchesPattern(line, pattern)) {
                    // 构建简化的调用栈
                    val callStack = callGraphBuilder.buildSimpleCallStack(content, lineNumber)

                    violations.add(
                        AudioViolation(
                            id = AudioViolation.generateId(),
                            severity = pattern.severity,
                            apiName = "${pattern.className}.${extractMethodName(line, pattern)}",
                            file = file.path,
                            line = lineNumber,
                            callStack = callStack,
                            suggestedFix = pattern.suggestion,
                            matchedPattern = pattern
                        )
                    )
                }
            }
        }

        return violations
    }

    /**
     * 匹配模式
     * Match pattern against line
     */
    private fun matchesPattern(line: String, pattern: AudioApiPattern): Boolean {
        // 忽略注释行
        // Skip comment lines
        if (line.trimStart().startsWith("//") || line.trimStart().startsWith("/*")) {
            return false
        }

        val regex = Regex(pattern.methodPattern, RegexOption.IGNORE_CASE)
        return regex.containsMatchIn(line)
    }

    /**
     * 从行中提取方法名
     * Extract method name from line
     */
    private fun extractMethodName(line: String, pattern: AudioApiPattern): String {
        val regex = Regex(pattern.methodPattern, RegexOption.IGNORE_CASE)
        val match = regex.find(line)
        return match?.value?.substringBefore("(") ?: pattern.methodPattern.substringBefore("(")
    }

    /**
     * 检查文件是否在排除列表中
     * Check if file is excluded
     */
    private fun isNotExcluded(file: File): Boolean {
        return excludePatterns.none { pattern ->
            val regexPattern = pattern.replace("**", ".*")
            file.path.matches(Regex(regexPattern))
        }
    }

    /**
     * 生成审计摘要
     * Generate audit summary
     */
    fun generateSummary(violations: List<AudioViolation>, affectedModules: List<String>): AuditSummary {
        return AuditSummary(
            totalViolations = violations.size,
            criticalCount = violations.count { it.severity == AudioApiSeverity.CRITICAL },
            warningCount = violations.count { it.severity == AudioApiSeverity.WARNING },
            infoCount = violations.count { it.severity == AudioApiSeverity.INFO },
            affectedModules = affectedModules.distinct()
        )
    }

    /**
     * 计算两次扫描之间的增量
     * Calculate delta between two scans
     */
    fun calculateDelta(
        previous: List<AudioViolation>,
        current: List<AudioViolation>
    ): ReportDelta {
        val previousIds = previous.map { "${it.file}:${it.line}:${it.apiName}" }.toSet()
        val currentIds = current.map { "${it.file}:${it.line}:${it.apiName}" }.toSet()

        val newIds = currentIds - previousIds
        val resolvedIds = previousIds - currentIds

        return ReportDelta(
            newViolations = current.filter { "${it.file}:${it.line}:${it.apiName}" in newIds },
            resolvedViolations = previous.filter { "${it.file}:${it.line}:${it.apiName}" in resolvedIds },
            unchangedViolations = current.filter { "${it.file}:${it.line}:${it.apiName}" !in newIds }
        )
    }
}
