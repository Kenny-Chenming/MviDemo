package com.gcp.agents.cli.auth

import java.io.FileInputStream
import java.io.IOException

/**
 * GcpAuthenticator — GCP 认证管理器
 */
class GcpAuthenticator {
    private var credentials: Credentials? = null

    fun withKeyFile(keyFilePath: String, scopes: List<String> = DEFAULT_SCOPES): GcpAuthenticator {
        this.credentials = KeyFileCredentials(keyFilePath)
        return this
    }

    fun withWorkloadIdentity(workloadPool: String, workloadProvider: String, serviceAccount: String, scopes: List<String> = DEFAULT_SCOPES): GcpAuthenticator {
        this.credentials = WorkloadIdentityCredentials(workloadPool, workloadProvider, serviceAccount)
        return this
    }

    fun withADC(scopes: List<String> = DEFAULT_SCOPES): GcpAuthenticator {
        this.credentials = AdcCredentials()
        return this
    }

    fun getCredentials(): Credentials {
        return credentials ?: throw GcpAuthException("Credentials not initialized")
    }

    fun getAccessToken(): String = getCredentials().getToken()

    fun validateCredentials(): Boolean {
        return try {
            getCredentials().getToken()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        val DEFAULT_SCOPES = listOf(
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/vertex-ai",
            "https://www.googleapis.com/auth/cloud-run",
            "https://www.googleapis.com/auth/container"
        )
        fun create(): GcpAuthenticator = GcpAuthenticator()
    }
}

interface Credentials { fun getToken(): String }

class KeyFileCredentials(private val keyFilePath: String) : Credentials {
    override fun getToken(): String {
        try {
            FileInputStream(keyFilePath).use { }
            return "ya29.dummy_token_from_${keyFilePath}"
        } catch (e: IOException) {
            throw GcpAuthException("Failed to load key file: $keyFilePath", e)
        }
    }
}

class WorkloadIdentityCredentials(private val workloadPool: String, private val workloadProvider: String, private val serviceAccount: String) : Credentials {
    override fun getToken(): String = "ya29.dummy_workload_identity_token"
}

class AdcCredentials : Credentials {
    override fun getToken(): String = "ya29.dummy_adc_token"
}

class GcpAuthException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
