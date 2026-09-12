package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.ConfigRepository
import com.example.model.LauncherConfig
import com.example.ui.LoadingScreen
import com.example.ui.LoginScreen
import com.example.ui.SettingsDialog
import com.example.util.GameTeleporter
import com.example.util.LaunchResult

enum class LauncherScreen {
    LOADING,
    LOGIN
}

class MainActivity : ComponentActivity() {
    private lateinit var configRepo: ConfigRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()

        configRepo = ConfigRepository(applicationContext)

        // Preload installed launchable apps in background thread during loading screen
        lifecycleScope.launch(Dispatchers.IO) {
            GameTeleporter.getInstalledLaunchableApps(applicationContext)
        }

        setContent {
            val config by configRepo.configFlow.collectAsState()
            var currentScreen by remember { mutableStateOf(LauncherScreen.LOADING) }
            var showSettingsDialog by remember { mutableStateOf(false) }
            var showAppNotFoundDialog by remember { mutableStateOf(false) }

            val accentColor = config.themeColor.getColor()

            val darkScheme = remember(accentColor) {
                darkColorScheme(
                    primary = accentColor,
                    secondary = accentColor,
                    background = Color(0xFF0A0E14),
                    surface = Color(0xFF101722)
                )
            }

            MaterialTheme(colorScheme = darkScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0E14)
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(600),
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            LauncherScreen.LOADING -> {
                                LoadingScreen(
                                    config = config,
                                    onLoadingComplete = {
                                        currentScreen = LauncherScreen.LOGIN
                                    }
                                )
                            }

                            LauncherScreen.LOGIN -> {
                                LoginScreen(
                                    config = config,
                                    onLoginSuccess = {
                                        // Attempt to launch target app
                                        val result = GameTeleporter.launchApp(
                                            context = this@MainActivity,
                                            targetPackage = config.targetPackageName
                                        )

                                        if (result is LaunchResult.Success) {
                                            // App launched
                                        } else {
                                            // If not installed on device, alert user with option to change target app
                                            showAppNotFoundDialog = true
                                        }
                                    },
                                    onOpenSettings = {
                                        showSettingsDialog = true
                                    },
                                    onUpdateTargetApp = { pkg, name ->
                                        configRepo.saveConfig(config.copy(targetPackageName = pkg, targetAppName = name))
                                        Toast.makeText(this@MainActivity, "Target app set to $name", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    // Settings Dialog (Accessible from top-right icon)
                    if (showSettingsDialog) {
                        SettingsDialog(
                            config = config,
                            onSaveConfig = { updated ->
                                configRepo.saveConfig(updated)
                            },
                            onImportImage = { uri, isForLoadingScreen ->
                                configRepo.importCustomImage(uri, isForLoadingScreen)
                            },
                            onClearCustomImage = { isForLoadingScreen ->
                                configRepo.clearCustomImage(isForLoadingScreen)
                            },
                            onResetDefaults = {
                                configRepo.resetToDefaults()
                            },
                            onDismiss = {
                                showSettingsDialog = false
                            }
                        )
                    }

                    // Not Installed Notice Dialog
                    if (showAppNotFoundDialog) {
                        AlertDialog(
                            onDismissRequest = { showAppNotFoundDialog = false },
                            title = {
                                Text(
                                    text = "Target App Not Installed",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp,
                                    color = accentColor
                                )
                            },
                            text = {
                                Text(
                                    text = "The configured target app '${config.targetAppName}' (${config.targetPackageName}) is not installed on this Android device/emulator.\n\nYou can select any installed app (e.g. Calculator, Google, or any game) in the Settings.",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showAppNotFoundDialog = false
                                        showSettingsDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                ) {
                                    Text("OPEN SETTINGS", color = Color.Black, fontFamily = FontFamily.Monospace)
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = { showAppNotFoundDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                ) {
                                    Text("DISMISS", color = Color.White, fontFamily = FontFamily.Monospace)
                                }
                            },
                            containerColor = Color(0xFF141C26)
                        )
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        // With BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE, tapping anywhere on the screen
        // will NEVER reveal the status bar (wifi, bluetooth, flashlight notification shade).
        // System bars only peek temporarily if swiped from the very edge.
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}
