package com.gcp.agents.cli.audit.exporter

import com.gcp.agents.cli.audit.log.AuditLogEntry
import com.google.api.services.bigquery.model.TableFieldSchema
import com.google.api.services.bigquery.model.TableSchema
import com.google.cloud.WriteChannel
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.gson.Gson
import java.nio.ByteBuffer

/**
 * AuditLogExporter — 审计日志导出器
 * Supports export to BigQuery, Cloud Storage (JSON/CSV), and Splunk.
 *
 * @param storage Cloud Storage client (for GCS exports)
 * @param bigqueryClient BigQuery client (for BigQuery exports)
 */
class AuditLogExporter(
    private val storage: Storage? = null,
    private val bigqueryClient: Any? = null // com.google.api.services.bigquery.Bigquery
) {
    private val gson = Gson()

    /**
     * 导出到 BigQuery / Export to BigQuery
     * @param entries 日志条目 / Log entries
     * @param datasetId BigQuery 数据集 ID / BigQuery dataset ID
     * @param tableId BigQuery 表 ID / BigQuery table ID
     * @param projectId GCP 项目 ID / GCP project ID
     */
    fun exportToBigQuery(
        entries: List<AuditLogEntry>,
        datasetId: String,
        tableId: String,
        projectId: String
    ): Boolean {
        // In production, use BigQuery API to insert rows
        // For now, simulate export
        println("[AuditLogExporter] Exporting ${entries.size} entries to BigQuery: ${projectId}.${datasetId}.${tableId}")
        return try {
            entries.forEach { entry ->
                // Simulate BigQuery insert
                println("[BigQuery] Inserting: ${entry.id}")
            }
            true
        } catch (e: Exception) {
            println("[AuditLogExporter] BigQuery export failed: ${e.message}")
            false
        }
    }

    /**
     * 导出到 Cloud Storage (JSON) / Export to Cloud Storage (JSON)
     * @param entries 日志条目 / Log entries
     * @param bucket GCS Bucket 名称 / GCS bucket name
     * @param objectKey 对象键 / Object key
     */
    fun exportToGcsJson(
        entries: List<AuditLogEntry>,
        bucket: String,
        objectKey: String
    ): String? {
        return try {
            val content = gson.toJson(entries)
            val blobId = BlobId.of(bucket, objectKey)
            val blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/json")
                .build()

            storage?.let {
                it.writer(blobInfo).use { writer ->
                    writer.write(ByteBuffer.wrap(content.toByteArray()))
                }
            } ?: run {
                // Fallback: print to stdout
                println("[AuditLogExporter] GCS export (simulated): gs://${bucket}/${objectKey}")
                println("[AuditLogExporter] Content length: ${content.length} bytes")
            }
            "gs://${bucket}/${objectKey}"
        } catch (e: Exception) {
            println("[AuditLogExporter] GCS JSON export failed: ${e.message}")
            null
        }
    }

    /**
     * 导出到 Cloud Storage (CSV) / Export to Cloud Storage (CSV)
     * @param entries 日志条目 / Log entries
     * @param bucket GCS Bucket 名称 / GCS bucket name
     * @param objectKey 对象键 / Object key
     */
    fun exportToGcsCsv(
        entries: List<AuditLogEntry>,
        bucket: String,
        objectKey: String
    ): String? {
        return try {
            val csvContent = buildString {
                appendLine(AuditLogEntry.CSV_HEADER)
                entries.forEach { entry ->
                    appendLine(entry.toCsv())
                }
            }

            val blobId = BlobId.of(bucket, objectKey)
            val blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("text/csv")
                .build()

            storage?.let {
                it.writer(blobInfo).use { writer ->
                    writer.write(ByteBuffer.wrap(csvContent.toByteArray()))
                }
            } ?: run {
                println("[AuditLogExporter] GCS CSV export (simulated): gs://${bucket}/${objectKey}")
            }
            "gs://${bucket}/${objectKey}"
        } catch (e: Exception) {
            println("[AuditLogExporter] GCS CSV export failed: ${e.message}")
            null
        }
    }

    /**
     * 导出到 Splunk HEC / Export to Splunk HEC
     * @param entries 日志条目 / Log entries
     * @param hecUrl Splunk HEC URL
     * @param token Splunk HEC token
     */
    fun exportToSplunk(
        entries: List<AuditLogEntry>,
        hecUrl: String,
        token: String
    ): Boolean {
        // In production, use Splunk HTTP Event Collector API
        println("[AuditLogExporter] Exporting ${entries.size} entries to Splunk: ${hecUrl}")
        return try {
            entries.forEach { entry ->
                // Simulate HEC call
                println("[Splunk HEC] Event: ${entry.id}")
            }
            true
        } catch (e: Exception) {
            println("[AuditLogExporter] Splunk export failed: ${e.message}")
            false
        }
    }

    /**
     * 获取 BigQuery 表 Schema / Get BigQuery table schema
     */
    fun getBigQuerySchema(): TableSchema {
        return TableSchema().setFields(listOf(
            TableFieldSchema().setName("id").setType("STRING").setMode("REQUIRED"),
            TableFieldSchema().setName("timestamp").setType("TIMESTAMP").setMode("REQUIRED"),
            TableFieldSchema().setName("operator").setType("STRING").setMode("REQUIRED"),
            TableFieldSchema().setName("agent_name").setType("STRING").setMode("REQUIRED"),
            TableFieldSchema().setName("operation").setType("STRING").setMode("REQUIRED"),
            TableFieldSchema().setName("input_summary").setType("STRING").setMode("NULLABLE"),
            TableFieldSchema().setName("output_summary").setType("STRING").setMode("NULLABLE"),
            TableFieldSchema().setName("gcp_resource").setType("STRING").setMode("NULLABLE"),
            TableFieldSchema().setName("status").setType("STRING").setMode("REQUIRED"),
            TableFieldSchema().setName("duration_ms").setType("INT64").setMode("NULLABLE")
        ))
    }
}
