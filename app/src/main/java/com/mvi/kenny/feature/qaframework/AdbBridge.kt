package com.mvi.kenny.feature.qaframework

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * ADB 桥接器 — 执行真实的 ADB 命令
 */
object AdbBridge {

    private const val TAG = "AdbBridge"

    // ADB 路径（从系统环境或已知路径获取）
    private val adbPath: String by lazy {
        System.getenv("ANDROID_HOME")?.let { "$it/platform-tools/adb" } 
            ?: "/Users/kenny/Library/Android/sdk/platform-tools/adb"
    }

    /**
     * 执行 ADB 命令，返回标准输出
     */
    suspend fun exec(vararg args: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder(listOf(adbPath) + args)
                .redirectErrorStream(true)
                .start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            val exitCode = process.waitFor()
            
            reader.close()
            
            if (exitCode == 0) {
                Result.success(output.trim())
            } else {
                Result.failure(Exception("ADB exit code: $exitCode\n$output"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "ADB exec failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 获取已连接的设备列表
     */
    suspend fun getDevices(): Result<List<AdbDevice>> = exec("devices").map { output ->
        output.lines()
            .drop(1) // 跳过 "List of devices attached"
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("*") }
            .mapNotNull { line ->
                val parts = line.split("\\s+".toRegex())
                if (parts.size >= 2) {
                    val id = parts[0]
                    val status = parts[1]
                    if (status == "device") {
                        // 获取设备名
                        val name = getDeviceProperty(id, "ro.product.model").getOrNull() ?: id
                        val version = getDeviceProperty(id, "ro.build.version.release").getOrNull() ?: "?"
                        AdbDevice(
                            id = id,
                            name = name,
                            androidVersion = "Android $version"
                        )
                    } else null
                } else null
            }
    }

    /**
     * 获取设备系统属性
     */
    suspend fun getDeviceProperty(deviceId: String, prop: String): Result<String> =
        exec("-s", deviceId, "shell", "getprop", prop)

    /**
     * 检查包是否已安装
     */
    suspend fun isPackageInstalled(deviceId: String, packageName: String): Result<Boolean> =
        exec("-s", deviceId, "shell", "pm", "list", "packages", packageName)
            .map { it.contains(packageName) }

    /**
     * 获取包的基本信息
     */
    suspend fun getPackageInfo(deviceId: String, packageName: String): Result<PackageInfo> {
        return exec("-s", deviceId, "shell", "dumpsys", "package", packageName).map { output ->
            val versionCode = Regex("versionCode=(\\d+)").find(output)?.groupValues?.get(1) ?: "?"
            val versionName = Regex("versionName=([^\\s]+)").find(output)?.groupValues?.get(1) ?: "?"
            val targetSdk = Regex("targetSdk=(\\d+)").find(output)?.groupValues?.get(1) ?: "?"
            val permissions = Regex("permission:\\s*([^\\s]+)").findAll(output).map { it.groupValues[1] }.toList()
            
            PackageInfo(
                packageName = packageName,
                versionCode = versionCode,
                versionName = versionName,
                targetSdk = targetSdk,
                permissions = permissions
            )
        }
    }

    /**
     * 截图并保存到指定路径
     */
    suspend fun takeScreenshot(deviceId: String, localPath: String): Result<String> {
        val remotePath = "/sdcard/screenshot_${System.currentTimeMillis()}.png"
        return exec("-s", deviceId, "shell", "screencap", "-p", remotePath)
            .recoverCatching { exec("-s", deviceId, "shell", "exec", "screencap", "-p", remotePath) }
            .recoverCatching { exec("-s", deviceId, "shell", "run-as", deviceId, "screencap", "-p", remotePath) }
            .mapCatching {
                // pull 文件到本地
                exec("-s", deviceId, "pull", remotePath, localPath)
                // 删除远程文件
                exec("-s", deviceId, "shell", "rm", remotePath)
                localPath
            }
    }

    /**
     * 启动指定包名的 App
     */
    suspend fun launchApp(deviceId: String, packageName: String): Result<String> {
        return exec("-s", deviceId, "shell", "monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1")
    }

    /**
     * 获取当前 Activity
     */
    suspend fun getCurrentActivity(deviceId: String): Result<String> =
        exec("-s", deviceId, "shell", "dumpsys", "activity", "activities")
            .map { output ->
                Regex("mResumedActivity:.*?\\{([^\\s]+)/([^\\s]+)\\}").find(output)?.let {
                    "${it.groupValues[1]}/${it.groupValues[2]}"
                } ?: ""
            }

    /**
     * dump UI 层次结构为 XML
     */
    suspend fun dumpUiHierarchy(deviceId: String): Result<String> =
        exec("-s", deviceId, "shell", "uiautomator", "dump", "/sdcard/ui_dump.xml")
            .recoverCatching { exec("-s", deviceId, "shell", "uiautomator", "dump") }
            .mapCatching {
                exec("-s", deviceId, "shell", "cat", "/sdcard/ui_dump.xml").getOrThrow()
            }

    /**
     * 按下 HOME 键
     */
    suspend fun pressHome(deviceId: String) = exec("-s", deviceId, "shell", "input", "keyevent", "KEYCODE_HOME")

    /**
     * 滑动屏幕
     */
    suspend fun swipe(deviceId: String, startX: Int, startY: Int, endX: Int, endY: Int, durationMs: Int = 300) =
        exec("-s", deviceId, "shell", "input", "swipe", "$startX", "$startY", "$endX", "$endY", "$durationMs")
}

data class AdbDevice(
    val id: String,
    val name: String,
    val androidVersion: String
)

data class PackageInfo(
    val packageName: String,
    val versionCode: String,
    val versionName: String,
    val targetSdk: String,
    val permissions: List<String>
)
