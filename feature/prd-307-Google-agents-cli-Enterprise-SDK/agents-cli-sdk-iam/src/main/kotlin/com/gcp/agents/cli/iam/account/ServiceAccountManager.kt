package com.gcp.agents.cli.iam.account

import com.google.api.services.iam.v1.Iam
import com.google.api.services.iam.v1.model.ServiceAccount as GoogleServiceAccount

/**
 * ServiceAccountManager — 管理 GCP Service Account，支持自动创建和轮换
 * Manages GCP Service Accounts with auto-creation and key rotation.
 *
 * @param iamClient GCP IAM API 客户端 / GCP IAM API client
 * @param projectId GCP 项目 ID / GCP project ID
 */
class ServiceAccountManager(
    private val iamClient: Iam,
    private val projectId: String
) {
    /**
     * 创建或获取现有的 Service Account / Create or get existing Service Account
     * @param name Service Account 名称 / Service account name
     * @param displayName 显示名称 / Display name
     * @return Service Account 邮箱 / Service account email
     */
    fun getOrCreateServiceAccount(name: String, displayName: String? = null): String {
        val email = "${name}@${projectId}.iam.gserviceaccount.com"

        try {
            iamClient.projects().serviceAccounts().get("projects/${projectId}/serviceAccounts/${email}").execute()
            return email
        } catch (e: Exception) {
            // Create if not exists
            return createServiceAccount(name, displayName ?: name)
        }
    }

    /**
     * 创建新的 Service Account / Create new Service Account
     * @param name Service Account 名称 / Service account name
     * @param displayName 显示名称 / Display name
     * @return Service Account 邮箱 / Service account email
     */
    fun createServiceAccount(name: String, displayName: String): String {
        val request = GoogleServiceAccount().apply {
            this.displayName = displayName
        }

        val result = iamClient.projects().serviceAccounts().create(
            "projects/${projectId}",
            request
        ).execute()

        return result.email
    }

    /**
     * 为 Service Account 创建密钥 / Create key for Service Account
     * @param serviceAccountEmail Service Account 邮箱 / Service account email
     * @return 密钥文件路径 / Key file path
     */
    fun createKey(serviceAccountEmail: String): String {
        val response = iamClient.projects().serviceAccounts().keys().create(
            "projects/${projectId}/serviceAccounts/${serviceAccountEmail}",
            null
        ).execute()

        return response.privateKeyData
    }

    /**
     * 轮换 Service Account 密钥 / Rotate Service Account key
     * @param serviceAccountEmail Service Account 邮箱 / Service account email
     * @param keyId 要轮换的密钥 ID / Key ID to rotate
     */
    fun rotateKey(serviceAccountEmail: String, keyId: String) {
        // Disable old key
        iamClient.projects().serviceAccounts().keys().disable(
            "projects/${projectId}/serviceAccounts/${serviceAccountEmail}/keys/${keyId}",
            null
        ).execute()

        // Create new key
        createKey(serviceAccountEmail)
    }

    /**
     * 删除 Service Account / Delete Service Account
     * @param serviceAccountEmail Service Account 邮箱 / Service account email
     */
    fun deleteServiceAccount(serviceAccountEmail: String) {
        iamClient.projects().serviceAccounts().delete(
            "projects/${projectId}/serviceAccounts/${serviceAccountEmail}"
        ).execute()
    }

    /**
     * 列出项目下的所有 Service Account / List all Service Accounts in project
     * @return Service Account 列表 / List of service accounts
     */
    fun listServiceAccounts(): List<GoogleServiceAccount> {
        val result = iamClient.projects().serviceAccounts().list(
            "projects/${projectId}"
        ).execute()

        return result.accounts ?: emptyList()
    }
}
