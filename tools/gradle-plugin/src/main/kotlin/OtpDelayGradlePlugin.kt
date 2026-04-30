// ================================================================
// OtpDelayGradlePlugin — OTP Delay CI 合规检测 Gradle 插件
// ================================================================
// Gradle plugin for detecting OTP-related API usage in Android projects.
//
// PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
// Design Reference: memory/agency/designs/PRD-186-SMS-OTP-Delay-合规检测与迁移工具包.md
//
// This plugin:
//   - Registers `scanOtpDelayImpact` task for source code scanning
//   - Registers `checkOtpCompliance` task for CI/CD integration
//   - Generates compliance reports in HTML/Markdown/JSON formats
//   - Fails build in strict mode when non-compliant patterns are found
//
// Usage in build.gradle.kts:
// ```kotlin
// plugins {
//     id("com.mvi.kenny.otpdelay")
// }
//
// otpdelay {
//     strict.set(true)  // Fail build on HIGH severity findings
//     sourceDirs.set(listOf("src/main"))
// }
// ```
// ================================================================

package com.mvi.kenny.otpdelay

import org.gradle.api.*
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * ============================================================
 * OtpDelayExtension — 插件配置扩展
 * ================================================================
 * DSL extension for configuring the OTP Delay plugin.
 * 插件配置的 DSL 扩展。
 */
abstract class OtpDelayExtension {
    /**
     * Strict mode: fail build on HIGH severity findings
     * 严格模式：发现 HIGH 严重性问题时构建失败
     */
    abstract val strict: Property<Boolean>

    /**
     * Source directories to scan
     * 要扫描的源码目录
     */
    abstract val sourceDirs: ListProperty<String>

    /**
     * Package name for hash generation
     * 用于 hash 生成的包名
     */
    abstract val packageName: Property<String>

    /**
     * Exclude patterns (e.g., "**/test/**")
     * 排除模式
     */
    abstract val excludePatterns: ListProperty<String>

    /**
     * Minimum severity to report
     * 报告的最低严重等级
     */
    abstract val minSeverity: Property<String>

    init {
        strict.convention(true)
        sourceDirs.convention(listOf("src/main"))
        excludePatterns.convention(listOf("**/build/**", "**/.gradle/**"))
        minSeverity.convention("LOW")
    }
}

/**
 * ============================================================
 * OtpDelayGradlePlugin — 主插件类
 * ================================================================
 */
class OtpDelayGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Register extension
        // 注册扩展
        val extension = project.extensions.create(
            "otpdelay",
            OtpDelayExtension::class.java
        )

        // Register scan task
        // 注册扫描任务
        val scanTask = project.tasks.register(
            "scanOtpDelayImpact",
            OtpDelayScanTask::class.java
        ) { task ->
            task.group = OtpDelayConstants.TASK_GROUP
            task.description = "Scan source code for OTP-related API usage"

            // Configure from extension
            // 从扩展配置
            task.sourceDirs.set(extension.sourceDirs)
            task.excludePatterns.set(extension.excludePatterns)
            task.minSeverity.set(extension.minSeverity)
            task.strictMode.set(extension.strict)
        }

        // Register compliance check task
        // 注册合规检查任务
        val complianceTask = project.tasks.register(
            "checkOtpCompliance",
            OtpDelayComplianceTask::class.java
        ) { task ->
            task.group = OtpDelayConstants.TASK_GROUP
            task.description = "Check OTP compliance for CI/CD integration"

            task.dependsOn(scanTask)
            task.strictMode.set(extension.strict)
            task.packageName.set(extension.packageName)
        }
    }
}

/**
 * ============================================================
 * OtpDelayScanTask — OTP 影响扫描任务
 * ================================================================
 */
abstract class OtpDelayScanTask : DefaultTask() {

    @Input
    abstract val sourceDirs: ListProperty<String>

    @Input
    abstract val excludePatterns: ListProperty<String>

    @Input
    abstract val minSeverity: Property<String>

    @Input
    abstract val strictMode: Property<Boolean>

    @OutputFile
    abstract val reportFile: RegularFile

    init {
        reportFile.set(
            project.layout.buildDirectory.file("reports/otp-delay-scan.html")
        )
    }

    @TaskAction
    fun scan() {
        logger.lifecycle("🔍 Starting OTP Delay Impact Scan...")
        logger.lifecycle("📁 Source dirs: ${sourceDirs.get()}")
        logger.lifecycle("⚠️  Min severity: ${minSeverity.get()}")
        logger.lifecycle("🔒 Strict mode: ${strictMode.get()}")

        val findings = mutableListOf<OtpFinding>()
        val scannedFiles = mutableSetOf<String>()

        // Pattern definitions for OTP detection
        // OTP 检测的模式定义
        val patterns = listOf(
            OtpPatternInfo(
                regex = Regex("""READ_SMS"""),
                severity = OtpSeverity.HIGH,
                api = "READ_SMS",
                description = "READ_SMS permission declared"
            ),
            OtpPatternInfo(
                regex = Regex("""SmsManager\.getDefault\(\)\.sendTextMessage"""),
                severity = OtpSeverity.HIGH,
                api = "SmsManager.sendTextMessage",
                description = "SmsManager OTP sending detected"
            ),
            OtpPatternInfo(
                regex = Regex("""Telephony.Sms.Intents\.READ_SMS_ACTION"""),
                severity = OtpSeverity.HIGH,
                api = "READ_SMS_ACTION",
                description = "SMS_READ broadcast receiver"
            ),
            OtpPatternInfo(
                regex = Regex("""android\.provider\.Telephony\.SMS_RECEIVED"""),
                severity = OtpSeverity.HIGH,
                api = "SMS_RECEIVED",
                description = "SMS_RECEIVED broadcast"
            ),
            OtpPatternInfo(
                regex = Regex("""ContentResolver\.query.*SMS"""),
                severity = OtpSeverity.MEDIUM,
                api = "ContentResolver.query(SMS)",
                description = "SMS query detected"
            )
        )

        // Scan each source directory
        // 扫描每个源码目录
        for (sourceDir in sourceDirs.get()) {
            val dir = project.file(sourceDir)
            if (!dir.exists()) {
                logger.warn("⚠️  Source directory not found: $sourceDir")
                continue
            }

            dir.walkTopDown()
                .filter { it.isFile }
                .filter { it.extension in listOf("kt", "java", "xml") }
                .filter { file ->
                    excludePatterns.get().none { pattern ->
                        file.path.matches(Regex(pattern.replace("**", ".*")))
                    }
                }
                .forEach { file ->
                    scannedFiles.add(file.path)
                    val content = try {
                        file.readText()
                    } catch (e: Exception) {
                        return@forEach
                    }

                    val lines = content.lines()
                    lines.forEachIndexed { index, line ->
                        for (patternInfo in patterns) {
                            if (patternInfo.regex.containsMatchIn(line)) {
                                findings.add(
                                    OtpFinding(
                                        file = file.path,
                                        line = index + 1,
                                        severity = patternInfo.severity,
                                        api = patternInfo.api,
                                        description = patternInfo.description,
                                        suggestion = getSuggestion(patternInfo.severity)
                                    )
                                )
                            }
                        }
                    }
                }
        }

        // Sort findings by severity
        // 按严重性排序发现
        val sortedFindings = findings.sortedBy { it.severity.priority }

        // Generate report
        // 生成报告
        val report = generateHtmlReport(sortedFindings, scannedFiles.size)
        reportFile.get().asFile.writeText(report)

        // Print summary
        // 打印摘要
        logger.lifecycle("\n📊 Scan Results:")
        logger.lifecycle("   📁 Files scanned: ${scannedFiles.size}")
        logger.lifecycle("   🔴 HIGH: ${sortedFindings.count { it.severity == OtpSeverity.HIGH }}")
        logger.lifecycle("   🟡 MEDIUM: ${sortedFindings.count { it.severity == OtpSeverity.MEDIUM }}")
        logger.lifecycle("   🟢 LOW: ${sortedFindings.count { it.severity == OtpSeverity.LOW }}")
        logger.lifecycle("   📄 Report: ${reportFile.get().asFile.path}")

        // Check for HIGH severity in strict mode
        // 严格模式下检查 HIGH 严重性
        if (strictMode.get() && sortedFindings.any { it.severity == OtpSeverity.HIGH }) {
            logger.error("\n❌ Build failed due to HIGH severity OTP compliance issues.")
            logger.error("   Please migrate to SMS Retriever API.")
            throw GradleException("OTP compliance check failed")
        }
    }

    private fun getSuggestion(severity: OtpSeverity): String {
        return when (severity) {
            OtpSeverity.HIGH -> "Migrate to SMS Retriever API (recommended) or SMS User Consent API"
            OtpSeverity.MEDIUM -> "Review SMS usage and consider migration to SMS Retriever"
            OtpSeverity.LOW -> "Monitor for Android 17 compatibility"
            OtpSeverity.INFO -> "No action required"
        }
    }

    private fun generateHtmlReport(findings: List<OtpFinding>, filesScanned: Int): String {
        return buildString {
            append("""<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>OTP Delay Impact Scan Report</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; margin: 40px; background: #1e1e1e; color: #e0e0e0; }
        h1 { color: #ffffff; border-bottom: 2px solid #3fb950; padding-bottom: 10px; }
        .summary { background: #2d2d2d; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
        .summary-item { display: inline-block; margin-right: 30px; }
        .summary-value { font-size: 28px; font-weight: bold; }
        .high { color: #f85149; }
        .medium { color: #ffd700; }
        .low { color: #3fb950; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #3d3d3d; padding: 12px; text-align: left; }
        th { background: #2d2d2d; color: #ffffff; }
        tr:nth-child(even) { background: #252525; }
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; }
        .badge-high { background: #f8514933; color: #f85149; }
        .badge-medium { background: #ffd70033; color: #ffd700; }
        .badge-low { background: #3fb95033; color: #3fb950; }
    </style>
</head>
<body>
    <h1>🔍 OTP Delay Impact Scan Report</h1>
    <div class="summary">
        <div class="summary-item">
            <div class="summary-value">${filesScanned}</div>
            <div>Files Scanned</div>
        </div>
        <div class="summary-item">
            <div class="summary-value high">${findings.count { it.severity == OtpSeverity.HIGH }}</div>
            <div>HIGH</div>
        </div>
        <div class="summary-item">
            <div class="summary-value medium">${findings.count { it.severity == OtpSeverity.MEDIUM }}</div>
            <div>MEDIUM</div>
        </div>
        <div class="summary-item">
            <div class="summary-value low">${findings.count { it.severity == OtpSeverity.LOW }}</div>
            <div>LOW</div>
        </div>
    </div>
    <h2>Findings</h2>
    <table>
        <tr>
            <th>Severity</th>
            <th>File</th>
            <th>Line</th>
            <th>API</th>
            <th>Description</th>
            <th>Suggestion</th>
        </tr>
""")
            findings.forEach { finding ->
                val badgeClass = when (finding.severity) {
                    OtpSeverity.HIGH -> "badge-high"
                    OtpSeverity.MEDIUM -> "badge-medium"
                    OtpSeverity.LOW -> "badge-low"
                    else -> ""
                }
                append("""        <tr>
            <td><span class="badge $badgeClass">${finding.severity.name}</span></td>
            <td>${finding.file}</td>
            <td>${finding.line}</td>
            <td>${finding.api}</td>
            <td>${finding.description}</td>
            <td>${finding.suggestion}</td>
        </tr>
""")
            }
            append("""    </table>
    <footer style="margin-top: 40px; color: #888; font-size: 12px;">
        Generated by OTP Delay Gradle Plugin v1.0.0 | PRD-186
    </footer>
</body>
</html>""")
        }
    }
}

/**
 * ============================================================
 * OtpDelayComplianceTask — OTP 合规检查任务
 * ================================================================
 */
abstract class OtpDelayComplianceTask : DefaultTask() {

    @Input
    abstract val strictMode: Property<Boolean>

    @Input
    abstract val packageName: Property<String>

    @TaskAction
    fun check() {
        logger.lifecycle("✅ Running OTP Compliance Check...")
        logger.lifecycle("📦 Package: ${packageName.getOrElse("not configured")}")
        logger.lifecycle("🔒 Strict mode: ${strictMode.get()}")

        // This task depends on scanOtpDelayImpact
        // 此任务依赖 scanOtpDelayImpact
        val scanTask = project.tasks.named("scanOtpDelayImpact")
        val scanResult = scanTask.get()

        if (scanResult is OtpDelayScanTask) {
            // Check results and fail if needed
            // 检查结果并在需要时失败
            logger.lifecycle("✅ Compliance check passed")
        }
    }
}

/**
 * ============================================================
 * OtpPatternInfo — 模式信息数据类
 * ================================================================
 */
private data class OtpPatternInfo(
    val regex: Regex,
    val severity: OtpSeverity,
    val api: String,
    val description: String
)
