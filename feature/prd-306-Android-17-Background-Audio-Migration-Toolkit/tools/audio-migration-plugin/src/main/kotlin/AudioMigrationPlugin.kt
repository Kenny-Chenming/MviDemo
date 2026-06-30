// ================================================================
// AudioMigrationPlugin — Android 17 后台音频迁移 Gradle 插件
// Android 17 Background Audio Migration Gradle Plugin
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// Gradle plugin that registers three tasks:
// 1. scanBackgroundAudio — 扫描后台音频违规
// 2. generateAudioMigration — 生成 WIU Service 脚手架
// 3. verifyAudioCompatibility — 运行兼容性测试
//
// Usage in build.gradle.kts:
// ```kotlin
// plugins {
//     id("com.mvi.kenny.audio-migration")
// }
//
// audioMigration {
//     targetApi.set(37)
//     modules.set(listOf("app", "feature-a"))
//     strictMode.set(true)
// }
// ```
// ================================================================

package com.mvi.kenny.audiobackground

import com.mvi.kenny.audiobackground.engine.*
import com.mvi.kenny.audiobackground.generator.*
import com.mvi.kenny.audiobackground.validation.*
import org.gradle.api.*
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * ================================================================
 * AudioMigrationExtension — 插件配置扩展
 * AudioMigrationExtension — Plugin Configuration Extension
 * ================================================================
 * DSL extension for configuring the audio migration plugin.
 * 插件配置的 DSL 扩展。
 */
abstract class AudioMigrationExtension {
    /** 目标 API 等级（默认 37） */
    abstract val targetApi: Property<Int>

    /** 要扫描的模块列表 */
    abstract val modules: ListProperty<String>

    /** 严格模式：发现 CRITICAL 违规时构建失败 */
    abstract val strictMode: Property<Boolean>

    /** 排除文件模式 */
    abstract val excludePatterns: ListProperty<String>

    /** 输出目录 */
    abstract val outputDir: Property<String>

    init {
        targetApi.convention(37)
        modules.convention(listOf("app"))
        strictMode.convention(true)
        excludePatterns.convention(listOf("**/build/**", "**/.gradle/**", "**/test/**"))
        outputDir.convention("build/reports")
    }
}

/**
 * ================================================================
 * AudioMigrationPlugin — 主插件类
 * AudioMigrationPlugin — Main Plugin Class
 * ================================================================
 */
class AudioMigrationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // 注册扩展
        // Register extension
        val extension = project.extensions.create(
            "audioMigration",
            AudioMigrationExtension::class.java
        )

        // 注册扫描任务
        // Register scan task
        val scanTask = project.tasks.register(
            "scanBackgroundAudio",
            AudioAuditTask::class.java
        ) { task ->
            task.group = "audio-migration"
            task.description = "Scan for background audio API violations (Android 17)"

            task.targetApi.set(extension.targetApi)
            task.sourceDirs.set(extension.modules.map { modules ->
                modules.map { "${project.project(it).projectDir}/src/main/kotlin" }
            })
            task.excludePatterns.set(extension.excludePatterns)
            task.strictMode.set(extension.strictMode)
        }

        // 注册脚手架生成任务
        // Register scaffold generation task
        val generateTask = project.tasks.register(
            "generateAudioMigration",
            AudioMigrationGeneratorTask::class.java
        ) { task ->
            task.group = "audio-migration"
            task.description = "Generate WIU Foreground Service scaffold code"

            task.projectDir.set(project.projectDir)
            task.outputDir.set(extension.outputDir)
        }

        // 注册兼容性测试任务
        // Register compatibility test task
        val verifyTask = project.tasks.register(
            "verifyAudioCompatibility",
            AudioCompatibilityTestTask::class.java
        ) { task ->
            task.group = "audio-migration"
            task.description = "Run Android 17 audio compatibility tests"

            task.strictMode.set(extension.strictMode)
        }
    }
}

/**
 * ================================================================
 * AudioAuditTask — 音频审计扫描任务
 * AudioAuditTask — Audio Audit Scan Task
 * ================================================================
 */
abstract class AudioAuditTask : DefaultTask() {

    @Input
    abstract val targetApi: Property<Int>

    @Input
    abstract val sourceDirs: ListProperty<String>

    @Input
    abstract val excludePatterns: ListProperty<String>

    @Input
    abstract val strictMode: Property<Boolean>

    @OutputFile
    abstract val htmlReportFile: RegularFile

    @OutputFile
    abstract val jsonReportFile: RegularFile

    init {
        htmlReportFile.set(project.layout.buildDirectory.file("reports/audio-audit.html"))
        jsonReportFile.set(project.layout.buildDirectory.file("reports/audio-audit.json"))
    }

    @TaskAction
    fun scan() {
        logger.lifecycle("\n🔍 PRD-306: Starting Background Audio Scan...")
        logger.lifecycle("   Target API: ${targetApi.get()}")
        logger.lifecycle("   Strict Mode: ${strictMode.get()}")
        logger.lifecycle("")

        val detector = BackgroundAudioDetector(
            targetApiLevel = targetApi.get(),
            excludePatterns = excludePatterns.get(),
            logger = { msg -> logger.lifecycle(msg) }
        )

        val violations = detector.scan(sourceDirs.get())
        val summary = detector.generateSummary(violations, sourceDirs.get())

        // 生成 HTML 报告
        // Generate HTML report
        val htmlReport = generateHtmlReport(violations, summary)
        htmlReportFile.get().asFile.writeText(htmlReport)

        // 生成 JSON 报告
        // Generate JSON report
        val jsonReport = generateJsonReport(violations, summary)
        jsonReportFile.get().asFile.writeText(jsonReport)

        logger.lifecycle("\n📊 Scan Summary:")
        logger.lifecycle("   Total Violations: ${summary.totalViolations}")
        logger.lifecycle("   🔴 CRITICAL: ${summary.criticalCount}")
        logger.lifecycle("   🟡 WARNING:  ${summary.warningCount}")
        logger.lifecycle("   🔵 INFO:     ${summary.infoCount}")
        logger.lifecycle("")
        logger.lifecycle("   📄 HTML Report: ${htmlReportFile.get().asFile.path}")
        logger.lifecycle("   📄 JSON Report: ${jsonReportFile.get().asFile.path}")

        // 严格模式下 CRITICAL 违规导致构建失败
        // CRITICAL violations fail build in strict mode
        if (strictMode.get() && summary.criticalCount > 0) {
            logger.error("\n❌ Build failed: ${summary.criticalCount} CRITICAL violations found.")
            logger.error("   Please migrate to WIU Foreground Service.")
            throw GradleException("Background audio compliance check failed: ${summary.criticalCount} CRITICAL violations")
        }
    }

    private fun generateHtmlReport(violations: List<AudioViolation>, summary: AuditSummary): String {
        val violationRows = violations.joinToString("\n") { v ->
            val badgeClass = when (v.severity) {
                AudioApiSeverity.CRITICAL -> "badge-critical"
                AudioApiSeverity.WARNING -> "badge-warning"
                AudioApiSeverity.INFO -> "badge-info"
            }
            val callStackHtml = if (v.callStack.isNotEmpty()) {
                "<details><summary>Call Stack</summary><pre>${v.callStack.joinToString("\n")}</pre></details>"
            } else "-"

            """
            <tr>
                <td><span class="badge $badgeClass">${v.severity.name}</span></td>
                <td><code>${v.apiName}</code></td>
                <td>${v.file}</td>
                <td>${v.line}</td>
                <td>$callStackHtml</td>
                <td>${v.suggestedFix}</td>
            </tr>
            """.trimIndent()
        }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Background Audio Audit Report - PRD-306</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; margin: 40px; background: #1e1e1e; color: #e0e0e0; }
        h1 { color: #ffffff; border-bottom: 2px solid #f85149; padding-bottom: 10px; }
        .summary { background: #2d2d2d; padding: 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 40px; }
        .summary-item { text-align: center; }
        .summary-value { font-size: 32px; font-weight: bold; }
        .critical { color: #f85149; }
        .warning { color: #ffd700; }
        .info { color: #58a6ff; }
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; }
        .badge-critical { background: #f8514933; color: #f85149; }
        .badge-warning { background: #ffd70033; color: #ffd700; }
        .badge-info { background: #58a6ff33; color: #58a6ff; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #3d3d3d; padding: 12px; text-align: left; vertical-align: top; }
        th { background: #2d2d2d; color: #ffffff; }
        tr:nth-child(even) { background: #252525; }
        code { background: #333; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
        pre { background: #333; padding: 8px; border-radius: 4px; font-size: 11px; }
        details { margin-top: 4px; }
    </style>
</head>
<body>
    <h1>🔊 Background Audio Audit Report</h1>
    <p>PRD-306: Android 17 Background Audio Migration Toolkit</p>
    
    <div class="summary">
        <div class="summary-item">
            <div class="summary-value">${summary.totalViolations}</div>
            <div>Total Violations</div>
        </div>
        <div class="summary-item">
            <div class="summary-value critical">${summary.criticalCount}</div>
            <div>CRITICAL</div>
        </div>
        <div class="summary-item">
            <div class="summary-value warning">${summary.warningCount}</div>
            <div>WARNING</div>
        </div>
        <div class="summary-item">
            <div class="summary-value info">${summary.infoCount}</div>
            <div>INFO</div>
        </div>
    </div>
    
    <h2>Violations (Sorted by Severity)</h2>
    <table>
        <tr>
            <th>Severity</th>
            <th>API</th>
            <th>File</th>
            <th>Line</th>
            <th>Call Stack</th>
            <th>Suggested Fix</th>
        </tr>
        $violationRows
    </table>
    
    <footer style="margin-top: 40px; color: #888; font-size: 12px;">
        Generated by PRD-306 Audio Migration Toolkit | ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}
    </footer>
</body>
</html>
        """.trimIndent()
    }

    private fun generateJsonReport(violations: List<AudioViolation>, summary: AuditSummary): String {
        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("  \"summary\": {")
        sb.appendLine("    \"totalViolations\": ${summary.totalViolations},")
        sb.appendLine("    \"criticalCount\": ${summary.criticalCount},")
        sb.appendLine("    \"warningCount\": ${summary.warningCount},")
        sb.appendLine("    \"infoCount\": ${summary.infoCount},")
        sb.appendLine("    \"timestamp\": ${summary.timestamp}")
        sb.appendLine("  },")
        sb.appendLine("  \"violations\": [")
        
        violations.forEachIndexed { index, v ->
            val comma = if (index < violations.size - 1) "," else ""
            sb.appendLine("    {")
            sb.appendLine("      \"id\": \"${v.id}\",")
            sb.appendLine("      \"severity\": \"${v.severity.name}\",")
            sb.appendLine("      \"apiName\": \"${v.apiName}\",")
            sb.appendLine("      \"file\": \"${v.file}\",")
            sb.appendLine("      \"line\": ${v.line},")
            sb.appendLine("      \"callStack\": [${v.callStack.joinToString(",") { "\"$it\"" }}],")
            sb.appendLine("      \"suggestedFix\": \"${v.suggestedFix}\"")
            sb.appendLine("    }$comma")
        }
        
        sb.appendLine("  ]")
        sb.appendLine("}")
        return sb.toString()
    }
}

/**
 * ================================================================
 * AudioMigrationGeneratorTask — 脚手架生成任务
 * AudioMigrationGeneratorTask — Scaffold Generation Task
 * ================================================================
 */
abstract class AudioMigrationGeneratorTask : DefaultTask() {

    @Input
    abstract val projectDir: Property<String>

    @Input
    abstract val outputDir: Property<String>

    @TaskAction
    fun generate() {
        logger.lifecycle("\n📦 PRD-306: Generating Audio Migration Scaffold...")

        val outputPath = "${projectDir.get()}/${outputDir.get()}"
        
        // 生成 WIU Service
        val serviceGenerator = WIUServiceGenerator()
        val service = serviceGenerator.generate()
        
        val serviceFile = java.io.File("$outputPath/AudioPlaybackService.kt")
        serviceFile.parentFile?.mkdirs()
        serviceFile.writeText(service.content)
        logger.lifecycle("   ✅ Generated: ${serviceFile.path}")

        // 生成 AudioFocusManager
        val focusManagerGenerator = AudioFocusManagerGenerator()
        val focusManager = focusManagerGenerator.generate()
        
        val focusManagerFile = java.io.File("$outputPath/AudioFocusManager.kt")
        focusManagerFile.writeText(focusManager.content)
        logger.lifecycle("   ✅ Generated: ${focusManagerFile.path}")

        // 生成迁移指南
        val guide = focusManagerGenerator.generateMigrationGuide()
        val guideFile = java.io.File("$outputPath/AudioMigrationGuide.md")
        guideFile.writeText(guide)
        logger.lifecycle("   ✅ Generated: ${guideFile.path}")

        // 生成 Gradle 配置修补建议
        val configPatcher = GradleConfigPatcher()
        val report = configPatcher.generateMigrationReport("app", emptyList())
        
        logger.lifecycle("\n📋 AndroidManifest.xml 需要添加的权限:")
        report.permissionsToAdd.forEach { p ->
            logger.lifecycle("   <uses-permission android:name=\"$p\" />")
        }
        
        logger.lifecycle("\n📋 AndroidManifest.xml 需要添加的 Service 声明:")
        logger.lifecycle("   <service android:name=\".AudioPlaybackService\"")
        logger.lifecycle("       android:foregroundServiceType=\"mediaPlayback\" />")

        logger.lifecycle("\n✅ Scaffold generation complete!")
        logger.lifecycle("   Output directory: $outputPath")
    }
}

/**
 * ================================================================
 * AudioCompatibilityTestTask — 兼容性测试任务
 * AudioCompatibilityTestTask — Compatibility Test Task
 * ================================================================
 */
abstract class AudioCompatibilityTestTask : DefaultTask() {

    @Input
    abstract val strictMode: Property<Boolean>

    @OutputFile
    abstract val reportFile: RegularFile

    init {
        reportFile.set(project.layout.buildDirectory.file("reports/audio-compatibility.html"))
    }

    @TaskAction
    fun verify() {
        logger.lifecycle("\n🧪 PRD-306: Running Android 17 Audio Compatibility Tests...")

        val runner = CompatibilityTestRunner()
        val result = runner.runDefaultTestSuite()

        val htmlReport = runner.generateHtmlReport(result)
        reportFile.get().asFile.writeText(htmlReport)

        logger.lifecycle("\n📊 Test Results:")
        logger.lifecycle("   Total Tests: ${result.totalTests}")
        logger.lifecycle("   ✅ Passed: ${result.passedTests}")
        logger.lifecycle("   ❌ Failed: ${result.failedTests}")
        logger.lifecycle("   Score: ${result.overallScore}%")
        logger.lifecycle("")
        logger.lifecycle("   📄 Report: ${reportFile.get().asFile.path}")

        // 列出失败的测试
        val failedTests = result.testResults.filter { !it.passed }
        if (failedTests.isNotEmpty()) {
            logger.lifecycle("\n❌ Failed Tests:")
            failedTests.forEach { ft ->
                logger.lifecycle("   - ${ft.testCase.name}: ${ft.errorMessage}")
            }
        }

        // 严格模式下测试失败导致构建失败
        if (strictMode.get() && !result.isPassed) {
            throw GradleException("Audio compatibility test failed: ${result.failedTests} tests failed")
        }
    }
}
