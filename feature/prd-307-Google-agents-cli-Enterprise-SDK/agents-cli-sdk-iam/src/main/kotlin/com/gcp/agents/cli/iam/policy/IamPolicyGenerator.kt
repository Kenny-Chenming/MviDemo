package com.gcp.agents.cli.iam.policy

import com.gcp.agents.cli.model.GcpIamPolicy

/**
 * IamPolicyGenerator — 生成 GCP IAM Policy YAML
 */
class IamPolicyGenerator(private val projectId: String) {

    fun generateVertexAiPolicy(policy: GcpIamPolicy): String = buildString {
        appendLine("# GCP IAM Policy for Vertex AI Agent")
        appendLine("version: 1")
        appendLine("bindings:")
        appendLine("  - role: roles/aiplatform.user")
        appendLine("    members:")
        policy.allowedResources.forEach { appendLine("      - $it") }
        if (policy.serviceAccountEmail != null) {
            appendLine("  - role: roles/iam.serviceAccountUser")
            appendLine("    members:")
            appendLine("      - serviceAccount:${policy.serviceAccountEmail}")
        }
    }

    fun generateCloudRunPolicy(policy: GcpIamPolicy, serviceName: String): String = buildString {
        appendLine("# GCP IAM Policy for Cloud Run Agent")
        appendLine("version: 1")
        appendLine("bindings:")
        appendLine("  - role: roles/run.invoker")
        appendLine("    members:")
        policy.allowedResources.forEach { appendLine("      - $it") }
    }

    fun generateGkePolicy(policy: GcpIamPolicy, cluster: String, namespace: String): String = buildString {
        appendLine("# GCP IAM Policy for GKE Agent")
        appendLine("version: 1")
        appendLine("bindings:")
        appendLine("  - role: roles/container.developer")
        appendLine("    members:")
        policy.allowedResources.forEach { appendLine("      - $it") }
    }

    fun generatePolicy(policy: GcpIamPolicy, targetService: String): String {
        return when {
            targetService.contains("vertex") || targetService.contains("aiplatform") -> generateVertexAiPolicy(policy)
            targetService.contains("run") -> generateCloudRunPolicy(policy, targetService)
            targetService.contains("gke") || targetService.contains("container") -> generateGkePolicy(policy, targetService, "default")
            else -> generateVertexAiPolicy(policy)
        }
    }
}
