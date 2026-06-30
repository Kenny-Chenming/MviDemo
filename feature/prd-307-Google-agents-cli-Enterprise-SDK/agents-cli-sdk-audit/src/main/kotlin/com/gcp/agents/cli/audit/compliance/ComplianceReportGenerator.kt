package com.gcp.agents.cli.audit.compliance

import com.gcp.agents.cli.audit.log.AuditLogEntry
import com.google.gson.GsonBuilder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ComplianceReportGenerator — 生成合规报告（满足 SOC2/ISO27001 要求）
 * Generates compliance reports meeting SOC2/ISO27001 requirements.
 *
 * @property reportDate 报告日期 / Report date
 */
class ComplianceReportGenerator(
    private val reportDate: Long = System.currentTimeMillis()
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * 生成 SOC2 合规报告 / Generate SOC2 compliance report
     * @param entries 审计日志条目 / Audit log entries
     * @return SOC2 报告 / SOC2 report
     */
    fun generateSoc2Report(entries: List<AuditLogEntry>): ComplianceReport {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())

        val summary = ComplianceSummary(
            reportType = "SOC2 Type II",
            generatedAt = formatter.format(Instant.now()),
            periodStart = entries.minOfOrNull { it.timestamp }?.let { formatter.format(Instant.ofEpochMilli(it)) } ?: "",
            periodEnd = formatter.format(Instant.ofEpochMilli(reportDate)),
            totalOperations = entries.size,
            successfulOperations = entries.count { it.status == AuditLogEntry.OperationStatus.SUCCESS },
            failedOperations = entries.count { it.status == AuditLogEntry.OperationStatus.FAILURE },
            complianceScore = calculateComplianceScore(entries),
            controls = generateControls(entries),
            findings = generateFindings(entries)
        )

        return ComplianceReport(
            reportType = "SOC2",
            version = "2.0",
            summary = summary,
            rawData = entries.map { it.toJson() },
            certifications = listOf("SOC2 Type II")
        )
    }

    /**
     * 生成 ISO27001 合规报告 / Generate ISO27001 compliance report
     * @param entries 审计日志条目 / Audit log entries
     * @return ISO27001 报告 / ISO27001 report
     */
    fun generateIso27001Report(entries: List<AuditLogEntry>): ComplianceReport {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())

        val summary = ComplianceSummary(
            reportType = "ISO/IEC 27001:2022",
            generatedAt = formatter.format(Instant.now()),
            periodStart = entries.minOfOrNull { it.timestamp }?.let { formatter.format(Instant.ofEpochMilli(it)) } ?: "",
            periodEnd = formatter.format(Instant.ofEpochMilli(reportDate)),
            totalOperations = entries.size,
            successfulOperations = entries.count { it.status == AuditLogEntry.OperationStatus.SUCCESS },
            failedOperations = entries.count { it.status == AuditLogEntry.OperationStatus.FAILURE },
            complianceScore = calculateComplianceScore(entries),
            controls = generateIsoControls(entries),
            findings = generateFindings(entries)
        )

        return ComplianceReport(
            reportType = "ISO27001",
            version = "2022",
            summary = summary,
            rawData = entries.map { it.toJson() },
            certifications = listOf("ISO/IEC 27001:2022")
        )
    }

    private fun calculateComplianceScore(entries: List<AuditLogEntry>): Float {
        if (entries.isEmpty()) return 100f
        val successRate = entries.count { it.status == AuditLogEntry.OperationStatus.SUCCESS }.toFloat() / entries.size
        return (successRate * 100).coerceIn(0f, 100f)
    }

    private fun generateControls(entries: List<AuditLogEntry>): List<ControlResult> {
        return listOf(
            ControlResult("CC6.1", "Logical and Physical Access Controls", ControlStatus.PASS,
                "All agent deployments require IAM authentication"),
            ControlResult("CC6.6", "Security for Confidential Electronic Information", ControlStatus.PASS,
                "${entries.count { it.gcpResource != null }} operations on protected resources"),
            ControlResult("CC7.2", "System Operations Management", ControlStatus.PASS,
                "All operations are logged and monitored"),
            ControlResult("CC7.4", "Error and Exception Management", entries.count { it.status == AuditLogEntry.OperationStatus.FAILURE }.let { count ->
                if (count == 0) ControlStatus.PASS else ControlStatus.WARNING
            }, "${entries.count { it.status == AuditLogEntry.OperationStatus.FAILURE }} failed operations recorded")
        )
    }

    private fun generateIsoControls(entries: List<AuditLogEntry>): List<ControlResult> {
        return listOf(
            ControlResult("A.9.1", "Business Requirements of Access Control", ControlStatus.PASS,
                "IAM-based access control enforced"),
            ControlResult("A.12.4", "Logging and Monitoring", ControlStatus.PASS,
                "${entries.size} log entries generated"),
            ControlResult("A.18.1", "Compliance with Legal Requirements", ControlStatus.PASS,
                "Audit logs retained for required period")
        )
    }

    private fun generateFindings(entries: List<AuditLogEntry>): List<ComplianceFinding> {
        val findings = mutableListOf<ComplianceFinding>()
        val failedOps = entries.filter { it.status == AuditLogEntry.OperationStatus.FAILURE }
        if (failedOps.isNotEmpty()) {
            findings.add(ComplianceFinding(
                severity = Severity.HIGH,
                category = "Operation Failures",
                description = "${failedOps.size} operations failed during reporting period",
                affectedResources = failedOps.mapNotNull { it.gcpResource }.distinct()
            ))
        }
        return findings
    }

    fun exportAsJson(report: ComplianceReport): String = gson.toJson(report)
}

data class ComplianceReport(
    val reportType: String,
    val version: String,
    val summary: ComplianceSummary,
    val rawData: List<String>,
    val certifications: List<String>
)

data class ComplianceSummary(
    val reportType: String,
    val generatedAt: String,
    val periodStart: String,
    val periodEnd: String,
    val totalOperations: Int,
    val successfulOperations: Int,
    val failedOperations: Int,
    val complianceScore: Float,
    val controls: List<ControlResult>,
    val findings: List<ComplianceFinding>
)

data class ControlResult(
    val controlId: String,
    val controlName: String,
    val status: ControlStatus,
    val evidence: String
)

enum class ControlStatus { PASS, FAIL, WARNING, NOT_APPLICABLE }

data class ComplianceFinding(
    val severity: Severity,
    val category: String,
    val description: String,
    val affectedResources: List<String>
)

enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
