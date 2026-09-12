package com.example.model

import androidx.compose.ui.graphics.Color
import org.json.JSONObject

enum class ThemeColor(
    val id: String,
    val displayName: String,
    val hexColor: String
) {
    GOLD("gold", "Tactical Gold", "#FFB000"),
    CYAN("cyan", "Cyber Cyan", "#00D2FF"),
    GREEN("green", "Matrix Green", "#00FF66"),
    PURPLE("purple", "Neon Violet", "#A855F7"),
    ORANGE("orange", "Flame Orange", "#FF6B00"),
    RED("red", "Crimson Red", "#EF4444");

    fun getColor(): Color = Color(android.graphics.Color.parseColor(hexColor))

    companion object {
        fun fromId(id: String): ThemeColor = entries.firstOrNull { it.id == id } ?: GOLD
    }
}

data class LauncherConfig(
    // Registration / Master Credentials
    val registeredUsername: String = "Player",
    val registeredPassword: String = "1234",

    // Loading Screen Configuration (8 seconds default, distinct background)
    val loadingDurationSeconds: Int = 8,
    val customLoadingBgPath: String? = null,
    val loadingBgPreset: String = "neon", // "neon", "dust", "dark"

    // Login Screen Configuration (distinct background)
    val customLoginBgPath: String? = null,
    val loginBgPreset: String = "dust", // "dust", "neon", "dark"

    // Target App (any app: calculator, google, games, cs1.6, etc.)
    val targetPackageName: String = "in.celest.xash3d.cs16client",
    val targetAppName: String = "CS 1.6 Client",

    // UI Accent Theme
    val themeColor: ThemeColor = ThemeColor.GOLD
) {
    fun toJsonString(): String {
        val json = JSONObject()
        json.put("registeredUsername", registeredUsername)
        json.put("registeredPassword", registeredPassword)
        json.put("loadingDurationSeconds", loadingDurationSeconds)
        json.put("customLoadingBgPath", customLoadingBgPath ?: "")
        json.put("loadingBgPreset", loadingBgPreset)
        json.put("customLoginBgPath", customLoginBgPath ?: "")
        json.put("loginBgPreset", loginBgPreset)
        json.put("targetPackageName", targetPackageName)
        json.put("targetAppName", targetAppName)
        json.put("themeColor", themeColor.id)
        return json.toString(2)
    }

    companion object {
        fun fromJsonString(jsonStr: String): LauncherConfig {
            return try {
                val json = JSONObject(jsonStr)
                val loadingPath = json.optString("customLoadingBgPath", "").takeIf { it.isNotEmpty() }
                val loginPath = json.optString("customLoginBgPath", "").takeIf { it.isNotEmpty() }
                LauncherConfig(
                    registeredUsername = json.optString("registeredUsername", "Player"),
                    registeredPassword = json.optString("registeredPassword", "1234"),
                    loadingDurationSeconds = json.optInt("loadingDurationSeconds", 8),
                    customLoadingBgPath = loadingPath,
                    loadingBgPreset = json.optString("loadingBgPreset", "neon"),
                    customLoginBgPath = loginPath,
                    loginBgPreset = json.optString("loginBgPreset", "dust"),
                    targetPackageName = json.optString("targetPackageName", "in.celest.xash3d.cs16client"),
                    targetAppName = json.optString("targetAppName", "CS 1.6 Client"),
                    themeColor = ThemeColor.fromId(json.optString("themeColor", ThemeColor.GOLD.id))
                )
            } catch (e: Exception) {
                LauncherConfig()
            }
        }
    }
}
