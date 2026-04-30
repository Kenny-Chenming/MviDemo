// ================================================================
// SmsRetrieverHashGenerator — SMS Retriever Hash 生成器 CLI 工具
// ================================================================
// Command-line tool for generating SMS Retriever hash strings.
//
// PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
// Design Reference: memory/agency/designs/PRD-186-SMS-OTP-Delay-合规检测与迁移工具包.md
//
// Usage:
//   java -jar hash-generator.jar --package com.example.app --keystore /path/to/keystore.jks --alias mykey
//
// The hash is computed as:
//   1. Get signing certificate's public key
//   2. Remove first byte (algorithm identifier: 30 81/82...)
//   3. Append package name bytes
//   4. SHA-256 hash
//   5. Take first 8 bytes, Base64 encode
// ================================================================

package com.mvi.kenny.otpdelay

import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.Certificate
import kotlin.system.exitProcess

/**
 * ============================================================
 * SmsRetrieverHashGenerator — Hash 生成器主类
 * ================================================================
 *
 * @param packageName App package name (required)
 * @param keystorePath Path to keystore file (optional for debug)
 * @param keyAlias Key alias in keystore (optional for debug)
 * @param keystorePassword Keystore password
 */
class SmsRetrieverHashGenerator(
    private val packageName: String,
    private val keystorePath: String? = null,
    private val keyAlias: String? = null,
    private val keystorePassword: CharArray = "android".toCharArray()
) {

    companion object {
        // Hash algorithm constants
        // Hash 算法常量
        private const val KEYSTORE_TYPE = "JKS"
        private const val DEFAULT_ALIAS = "androiddebugkey"
        private const val DEBUG_KEYSTORE_PATH = "~/.android/debug.keystore"
    }

    /**
     * Generate the SMS Retriever hash string
     * 生成 SMS Retriever hash 字符串
     *
     * @return Base64-encoded hash string in format: "PACKAGE_NAME:HASH"
     * @throws IllegalArgumentException if required parameters are missing
     */
    fun generateHash(): String {
        require(packageName.isNotBlank()) { "Package name is required" }

        // Get the signing certificate
        // 获取签名证书
        val certificate = getSigningCertificate()

        // Extract public key bytes
        // 提取公钥字节
        val publicKeyBytes = extractPublicKeyBytes(certificate)

        // Compute hash: SHA-256(publicKeyBytes + packageNameBytes)
        // 计算 hash：SHA-256(公钥字节 + 包名字节)
        val hashBytes = computeHash(publicKeyBytes, packageName.toByteArray())

        // Base64 encode first 8 bytes
        // Base64 编码前 8 字节
        val base64Hash = base64Encode(hashBytes.take(8).toByteArray())

        return "$packageName:$base64Hash"
    }

    /**
     * Get signing certificate from keystore or debug keystore
     * 从 keystore 或 debug keystore 获取签名证书
     */
    private fun getSigningCertificate(): Certificate {
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)

        val actualKeystorePath = keystorePath ?: getDefaultDebugKeystorePath()
        val actualKeyAlias = keyAlias ?: DEFAULT_ALIAS

        keyStore.load(
            java.io.FileInputStream(
                expandUserHome(actualKeystorePath)
            ),
            keystorePassword
        )

        return keyStore.getCertificate(actualKeyAlias)
    }

    /**
     * Extract public key bytes from certificate, removing algorithm identifier
     * 从证书提取公钥字节，移除算法标识符
     *
     * The X.509 SubjectPublicKeyInfo structure starts with:
     *   30 81/82 ... — SEQUENCE identifier + length
     * We need to remove this prefix to get just the raw public key.
     */
    private fun extractPublicKeyBytes(certificate: Certificate): ByteArray {
        val encodedKey = certificate.publicKey.encoded

        // Find the actual key data start (skip ASN.1 header)
        // 找到实际密钥数据起始位置（跳过 ASN.1 头）
        var keyStartIndex = 0
        var sequenceLength = 0

        // Check for SEQUENCE tag (0x30)
        if (encodedKey[0] == 0x30.toByte()) {
            val lengthByte = encodedKey[1].toInt() and 0xFF

            // Handle long form length
            // 处理长形式长度
            if (lengthByte and 0x80 != 0) {
                val numLengthBytes = lengthByte and 0x7F
                var multiplier = 1
                sequenceLength = 0
                for (i in numLengthBytes downTo 1) {
                    sequenceLength += (encodedKey[1 + i].toInt() and 0xFF) * multiplier
                    multiplier *= 256
                }
                keyStartIndex = 2 + numLengthBytes
            } else {
                sequenceLength = lengthByte
                keyStartIndex = 2
            }
        }

        // Remove the algorithm identifier header
        // The structure is: SEQUENCE { algorithmIdentifier, subjectPublicKey }
        // We want to keep the subjectPublicKey part
        val headerLength = keyStartIndex

        // Skip algorithm identifier (typically 15-20 bytes for RSA)
        // 跳过算法标识符（RSA 通常 15-20 字节）
        var keyDataStart = headerLength
        var identifierLength = 0

        if (encodedKey[keyDataStart] == 0x30.toByte()) {
            val algoLengthByte = encodedKey[keyDataStart + 1].toInt() and 0xFF
            if (algoLengthByte and 0x80 != 0) {
                val numBytes = algoLengthByte and 0x7F
                var multiplier = 1
                identifierLength = 0
                for (i in numBytes downTo 1) {
                    identifierLength += (encodedKey[keyDataStart + 1 + i].toInt() and 0xFF) * multiplier
                    multiplier *= 256
                }
                identifierLength += numBytes + 2 // +2 for tag and length bytes
            } else {
                identifierLength = algoLengthByte + 2
            }
        }

        val actualKeyStart = keyDataStart + identifierLength
        val keyLength = encodedKey.size - actualKeyStart

        return encodedKey.copyOfRange(actualKeyStart, actualKeyStart + keyLength)
    }

    /**
     * Compute SHA-256 hash of the combined data
     * 计算合并数据的 SHA-256 hash
     */
    private fun computeHash(varray: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(varray)
    }

    /**
     * Compute SHA-256 hash of public key + package name
     * 计算公钥 + 包名的 SHA-256 hash
     */
    private fun computeHash(publicKeyBytes: ByteArray, packageNameBytes: ByteArray): ByteArray {
        val combined = publicKeyBytes + packageNameBytes
        return computeHash(combined)
    }

    /**
     * Base64 encode bytes
     * Base64 编码字节
     */
    private fun base64Encode(bytes: ByteArray): String {
        return java.util.Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * Get default debug keystore path
     * 获取默认 debug keystore 路径
     */
    private fun getDefaultDebugKeystorePath(): String {
        return expandUserHome(DEBUG_KEYSTORE_PATH)
    }

    /**
     * Expand ~ to user home directory
     * 将 ~ 展开为用户主目录
     */
    private fun expandUserHome(path: String): String {
        return if (path.startsWith("~")) {
            System.getProperty("user.home") + path.substring(1)
        } else {
            path
        }
    }
}

/**
 * ============================================================
 * main — CLI 入口点
 * ================================================================
 */
fun main(args: Array<String>) {
    var packageName: String? = null
    var keystorePath: String? = null
    var keyAlias: String? = null
    var keystorePassword: String = "android"

    // Parse command line arguments
    // 解析命令行参数
    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--package" -> {
                packageName = args.getOrNull(++i)
            }
            "--keystore" -> {
                keystorePath = args.getOrNull(++i)
            }
            "--alias" -> {
                keyAlias = args.getOrNull(++i)
            }
            "--password" -> {
                keystorePassword = args.getOrNull(++i) ?: keystorePassword
            }
            "--help" -> {
                printUsage()
                exitProcess(0)
            }
        }
        i++
    }

    // Validate required parameters
    // 验证必需参数
    if (packageName == null) {
        System.err.println("Error: --package is required")
        System.err.println()
        printUsage()
        exitProcess(1)
    }

    try {
        val generator = SmsRetrieverHashGenerator(
            packageName = packageName,
            keystorePath = keystorePath,
            keyAlias = keyAlias,
            keystorePassword = keystorePassword.toCharArray()
        )

        val hash = generator.generateHash()
        println()
        println("✅ SMS Retriever Hash generated successfully!")
        println()
        println("📱 Add this hash to your SMS Retriever backend:")
        println()
        println("   $hash")
        println()
        println("📋 In your server-side SMS message, include:")
        println("   <##> your_app_hash $hash </#>")
        println()

    } catch (e: Exception) {
        System.err.println()
        System.err.println("❌ Error generating hash: ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
}

/**
 * Print usage information
 * 打印使用说明
 */
private fun printUsage() {
    println("""
SMS Retriever Hash Generator
============================

Usage:
  java -jar hash-generator.jar --package <package_name> [options]

Required:
  --package <name>    Your app's package name (e.g., com.example.myapp)

Options:
  --keystore <path>   Path to keystore file (optional for debug builds)
  --alias <name>      Key alias in keystore (default: androiddebugkey)
  --password <pwd>    Keystore password (default: android)
  --help              Show this help message

Examples:
  # Debug build (uses default debug keystore)
  java -jar hash-generator.jar --package com.example.myapp

  # Release build
  java -jar hash-generator.jar \
    --package com.example.myapp \
    --keystore /path/to/my-release-key.jks \
    --alias my-key-alias \
    --password mypassword

For more information, see:
https://developers.google.com/identity/sms-retriever/hash

    """.trimIndent())
}
