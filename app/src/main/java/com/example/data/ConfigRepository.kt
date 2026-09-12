package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.model.LauncherConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

class ConfigRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_launcher_prefs", Context.MODE_PRIVATE)
    private val keyConfigJson = "launcher_config_v2"

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<LauncherConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): LauncherConfig {
        val savedJson = prefs.getString(keyConfigJson, null)
        return if (savedJson != null) {
            LauncherConfig.fromJsonString(savedJson)
        } else {
            LauncherConfig()
        }
    }

    fun saveConfig(config: LauncherConfig) {
        prefs.edit().putString(keyConfigJson, config.toJsonString()).apply()
        _configFlow.value = config
    }

    fun resetToDefaults(): LauncherConfig {
        // Clear any saved custom images
        try {
            File(context.filesDir, "custom_loading_bg.jpg").delete()
            File(context.filesDir, "custom_login_bg.jpg").delete()
        } catch (_: Exception) {}

        val defaultConfig = LauncherConfig()
        saveConfig(defaultConfig)
        return defaultConfig
    }

    fun importCustomImage(uri: Uri, isForLoadingScreen: Boolean): String? {
        return try {
            val fileName = if (isForLoadingScreen) "custom_loading_bg.jpg" else "custom_login_bg.jpg"
            val destFile = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val absolutePath = destFile.absolutePath
            val current = _configFlow.value
            val updated = if (isForLoadingScreen) {
                current.copy(customLoadingBgPath = absolutePath)
            } else {
                current.copy(customLoginBgPath = absolutePath)
            }
            saveConfig(updated)
            absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun clearCustomImage(isForLoadingScreen: Boolean) {
        try {
            val fileName = if (isForLoadingScreen) "custom_loading_bg.jpg" else "custom_login_bg.jpg"
            File(context.filesDir, fileName).delete()
        } catch (_: Exception) {}

        val current = _configFlow.value
        val updated = if (isForLoadingScreen) {
            current.copy(customLoadingBgPath = null)
        } else {
            current.copy(customLoginBgPath = null)
        }
        saveConfig(updated)
    }
}
