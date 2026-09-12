package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class LaunchResult {
    data class Success(val packageName: String) : LaunchResult()
    data class NotInstalled(val targetPackage: String, val suggestedAlternatives: List<String>) : LaunchResult()
    data class Error(val message: String) : LaunchResult()
}

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val iconBitmap: Bitmap? = null
)

object GameTeleporter {
    private const val TAG = "GameTeleporter"

    @Volatile
    private var cachedApps: List<InstalledAppInfo> = emptyList()

    fun getCachedApps(): List<InstalledAppInfo> = cachedApps

    val POPULAR_CS_PACKAGES = listOf(
        "in.celest.xash3d.cs16client" to "CS 1.6 Client (Standard Android)",
        "in.celest.xash3d.cs16client.google" to "CS 1.6 Client (Play Store)",
        "in.celest.xash3d.hl" to "Xash3D FWGS (Half-Life Engine)",
        "org.xash.cs" to "CS 1.6 Client (Community)"
    )

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchApp(context: Context, targetPackage: String): LaunchResult {
        val pm = context.packageManager

        // 1. First attempt: standard getLaunchIntentForPackage
        try {
            val intent = pm.getLaunchIntentForPackage(targetPackage)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
                return LaunchResult.Success(targetPackage)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Standard launch intent failed for $targetPackage, trying fallback", e)
        }

        // 2. Second attempt: query launcher intent activity explicitly
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(targetPackage)
            }
            val activities = pm.queryIntentActivities(mainIntent, 0)
            if (activities.isNotEmpty()) {
                val act = activities[0]
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(targetPackage, act.activityInfo.name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
                context.startActivity(intent)
                return LaunchResult.Success(targetPackage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback launcher search failed for $targetPackage", e)
        }

        if (!isPackageInstalled(context, targetPackage)) {
            return LaunchResult.NotInstalled(targetPackage, emptyList())
        }

        return LaunchResult.Error("Could not create launch intent for $targetPackage")
    }

    fun findInstalledCsPackage(context: Context): String? {
        for ((pkg, _) in POPULAR_CS_PACKAGES) {
            if (isPackageInstalled(context, pkg)) {
                return pkg
            }
        }
        return null
    }

    fun launchGame(
        context: Context,
        targetPackage: String,
        arguments: String = "-game cstrike",
        username: String = "Player"
    ): LaunchResult {
        val packageToLaunch = if (isPackageInstalled(context, targetPackage)) {
            targetPackage
        } else {
            findInstalledCsPackage(context)
        }

        if (packageToLaunch == null) {
            val installedAlternatives = cachedApps
                .map { it.packageName }
                .take(5)
            return LaunchResult.NotInstalled(
                targetPackage = targetPackage,
                suggestedAlternatives = installedAlternatives
            )
        }

        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageToLaunch)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

                // Inject tactical game arguments
                val resolvedArgs = arguments.replace("\$USERNAME", username)
                intent.putExtra("argv", resolvedArgs)
                intent.putExtra("-game", "cstrike")
                intent.putExtra("+name", username)

                context.startActivity(intent)
                LaunchResult.Success(packageToLaunch)
            } else {
                LaunchResult.Error("Could not create launch intent for $packageToLaunch")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch $packageToLaunch", e)
            LaunchResult.Error(e.message ?: "Failed to launch game")
        }
    }

    suspend fun getInstalledLaunchableApps(context: Context, forceRefresh: Boolean = false): List<InstalledAppInfo> =
        withContext(Dispatchers.IO) {
            if (!forceRefresh && cachedApps.isNotEmpty()) {
                return@withContext cachedApps
            }
            try {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                val apps = resolveInfos.mapNotNull { resolveInfo ->
                    val pkgName = resolveInfo.activityInfo.packageName
                    if (pkgName != context.packageName) {
                        val label = resolveInfo.loadLabel(pm).toString()
                        val iconDrawable = try { resolveInfo.loadIcon(pm) } catch (e: Exception) { null }
                        val bitmap = drawableToBitmap(iconDrawable)
                        InstalledAppInfo(packageName = pkgName, appName = label, iconBitmap = bitmap)
                    } else null
                }.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }

                cachedApps = apps
                apps
            } catch (e: Exception) {
                Log.e(TAG, "Failed to query launchable apps", e)
                emptyList()
            }
        }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        return try {
            val width = if (drawable.intrinsicWidth in 1..256) drawable.intrinsicWidth else 96
            val height = if (drawable.intrinsicHeight in 1..256) drawable.intrinsicHeight else 96
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun openPlayStoreOrBrowserForCs16(context: Context, packageName: String = "in.celest.xash3d.cs16client") {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/FWGS/cs16-client")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
