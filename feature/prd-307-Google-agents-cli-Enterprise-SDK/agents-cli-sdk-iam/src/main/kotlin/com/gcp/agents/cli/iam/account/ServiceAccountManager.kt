package com.gcp.agents.cli.iam.account

/**
 * ServiceAccountManager — 管理 GCP Service Account
 */
class ServiceAccountManager(private val projectId: String) {
    private val serviceAccounts = mutableMapOf<String, String>()

    fun getOrCreateServiceAccount(name: String, displayName: String? = null): String {
        val email = "${name}@${projectId}.iam.gserviceaccount.com"
        if (serviceAccounts.containsKey(name)) return email
        return createServiceAccount(name, displayName ?: name)
    }

    fun createServiceAccount(name: String, displayName: String): String {
        val email = "${name}@${projectId}.iam.gserviceaccount.com"
        serviceAccounts[name] = email
        return email
    }

    fun createKey(serviceAccountEmail: String): String {
        return "/tmp/${serviceAccountEmail.replace("@", "_").replace(".", "_")}_key.json"
    }

    fun rotateKey(serviceAccountEmail: String, keyId: String) {
        // In production: call GCP IAM API
    }

    fun deleteServiceAccount(serviceAccountEmail: String) {
        val name = serviceAccountEmail.substringBefore("@").substringBefore("@${projectId}.iam.gserviceaccount.com")
        serviceAccounts.remove(name)
    }

    fun listServiceAccounts(): List<ServiceAccountInfo> {
        return serviceAccounts.map { (name, email) ->
            ServiceAccountInfo(name, email, "Created by agents-cli-sdk")
        }
    }
}

data class ServiceAccountInfo(val name: String, val email: String, val displayName: String)
