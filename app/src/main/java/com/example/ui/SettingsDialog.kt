package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.model.LauncherConfig
import com.example.model.ThemeColor
import com.example.util.GameTeleporter
import com.example.util.InstalledAppInfo
import java.io.File

@Composable
fun SettingsDialog(
    config: LauncherConfig,
    onSaveConfig: (LauncherConfig) -> Unit,
    onImportImage: (Uri, Boolean) -> Unit, // uri, isForLoadingScreen
    onClearCustomImage: (Boolean) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    var username by remember { mutableStateOf(config.registeredUsername) }
    var password by remember { mutableStateOf(config.registeredPassword) }
    var targetPackage by remember { mutableStateOf(config.targetPackageName) }
    var targetAppName by remember { mutableStateOf(config.targetAppName) }
    var loadingSeconds by remember { mutableIntStateOf(config.loadingDurationSeconds) }
    var themeColor by remember { mutableStateOf(config.themeColor) }
    var loadingBgPreset by remember { mutableStateOf(config.loadingBgPreset) }
    var loginBgPreset by remember { mutableStateOf(config.loginBgPreset) }
    var showAppPickerDialog by remember { mutableStateOf(false) }

    val accentColor = themeColor.getColor()

    // Photo Pickers
    val loadingImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImportImage(uri, true)
        }
    }

    val loginImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImportImage(uri, false)
        }
    }

    // Installed apps
    var installedApps by remember { mutableStateOf(GameTeleporter.getCachedApps()) }
    var appSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (installedApps.isEmpty()) {
            installedApps = GameTeleporter.getInstalledLaunchableApps(context)
        }
    }

    val filteredApps = remember(installedApps, appSearchQuery) {
        if (appSearchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.appName.contains(appSearchQuery, ignoreCase = true) ||
                    it.packageName.contains(appSearchQuery, ignoreCase = true)
        }
    }

    fun applyAndClose() {
        val updated = config.copy(
            registeredUsername = username.trim(),
            registeredPassword = password,
            targetPackageName = targetPackage.trim(),
            targetAppName = targetAppName.trim(),
            loadingDurationSeconds = loadingSeconds,
            themeColor = themeColor,
            loadingBgPreset = loadingBgPreset,
            loginBgPreset = loginBgPreset
        )
        onSaveConfig(updated)
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .clip(CutCornerShape(10.dp))
                .border(2.dp, accentColor, CutCornerShape(10.dp)),
            color = Color(0xFF0D1219)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141C26))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LAUNCHER SETTINGS & ASSETS",
                            color = accentColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Tabs (Horizontal in Landscape)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F1621),
                    contentColor = accentColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = accentColor,
                            height = 3.dp
                        )
                    }
                ) {
                    val tabTitles = listOf("REGISTER & ACCOUNT", "CUSTOM PICTURES & UI", "TARGET APP MODIFIER")
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        )
                    }
                }

                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // TAB 1: REGISTER USERNAME & PASSWORD
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "REGISTER OPERATOR ACCOUNT",
                                    color = accentColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Configure the username and password required to unlock and launch the target app. If the entered password doesn't match this, the login screen will show 'Wrong password'.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    label = { Text("Registered Username", fontFamily = FontFamily.Monospace) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = accentColor) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentColor,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF131A24),
                                        unfocusedContainerColor = Color(0xFF101620)
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Registered Password", fontFamily = FontFamily.Monospace) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentColor,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF131A24),
                                        unfocusedContainerColor = Color(0xFF101620)
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Loading duration
                                Text(
                                    text = "LOADING SCREEN DURATION: ${loadingSeconds}s",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Slider(
                                    value = loadingSeconds.toFloat(),
                                    onValueChange = { loadingSeconds = it.toInt() },
                                    valueRange = 2f..15f,
                                    steps = 12,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accentColor,
                                        activeTrackColor = accentColor
                                    )
                                )
                            }
                        }

                        1 -> {
                            // TAB 2: CUSTOM PICTURES & UI CUSTOMIZATION
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "CUSTOM BACKGROUND PICTURES (DIFFERENT FOR EACH SCREEN)",
                                    color = accentColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Import picture files directly from your phone storage to set unique backgrounds for the Loading Screen and Login Screen.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // --- SECTION A: LOADING SCREEN BACKGROUND ---
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CutCornerShape(6.dp))
                                        .background(Color(0xFF141C26))
                                        .border(1.dp, Color(0xFF2C394B), CutCornerShape(6.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail preview
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp, 50.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black)
                                    ) {
                                        if (config.customLoadingBgPath != null && File(config.customLoadingBgPath).exists()) {
                                            AsyncImage(
                                                model = File(config.customLoadingBgPath),
                                                contentDescription = "Custom Loading BG",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            val res = if (loadingBgPreset == "dust") R.drawable.img_cs_dust_banner_1789190469975 else R.drawable.img_loading_bg_1789191723589
                                            Image(
                                                painter = painterResource(id = res),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "1. Loading Screen Background",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = if (config.customLoadingBgPath != null) "Custom Image File Imported" else "Preset: $loadingBgPreset",
                                            color = accentColor,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            loadingImagePicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                        shape = CutCornerShape(4.dp)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("IMPORT FILE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }

                                    if (config.customLoadingBgPath != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { onClearCustomImage(true) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // --- SECTION B: LOGIN SCREEN BACKGROUND ---
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CutCornerShape(6.dp))
                                        .background(Color(0xFF141C26))
                                        .border(1.dp, Color(0xFF2C394B), CutCornerShape(6.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail preview
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp, 50.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black)
                                    ) {
                                        if (config.customLoginBgPath != null && File(config.customLoginBgPath).exists()) {
                                            AsyncImage(
                                                model = File(config.customLoginBgPath),
                                                contentDescription = "Custom Login BG",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            val res = if (loginBgPreset == "neon") R.drawable.img_loading_bg_1789191723589 else R.drawable.img_cs_dust_banner_1789190469975
                                            Image(
                                                painter = painterResource(id = res),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "2. Login Screen Background",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = if (config.customLoginBgPath != null) "Custom Image File Imported" else "Preset: $loginBgPreset",
                                            color = accentColor,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            loginImagePicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                        shape = CutCornerShape(4.dp)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("IMPORT FILE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }

                                    if (config.customLoginBgPath != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { onClearCustomImage(false) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // --- SECTION C: UI ACCENT COLOR ---
                                Text(
                                    text = "UI ACCENT COLOR THEME",
                                    color = accentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ThemeColor.entries.forEach { tc ->
                                        val isSelected = themeColor == tc
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(CutCornerShape(4.dp))
                                                .background(if (isSelected) Color(0xFF243242) else Color(0xFF131A24))
                                                .border(
                                                    1.5.dp,
                                                    if (isSelected) tc.getColor() else Color(0xFF2C394B),
                                                    CutCornerShape(4.dp)
                                                )
                                                .clickable { themeColor = tc }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(tc.getColor())
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = tc.displayName,
                                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // TAB 3: TARGET APP MODIFIER (Calculator, Google, Games, CS1.6, etc.)
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "MODIFY TARGET APPLICATION",
                                    color = accentColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "After successful login, the launcher will immediately launch this app. Click 'Pick target apps' to choose any installed app from your phone:",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Button(
                                    onClick = { showAppPickerDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    shape = CutCornerShape(4.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pick target apps",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = targetAppName,
                                        onValueChange = { targetAppName = it },
                                        label = { Text("Display App Name", fontFamily = FontFamily.Monospace) },
                                        modifier = Modifier.weight(0.4f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = accentColor,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF131A24),
                                            unfocusedContainerColor = Color(0xFF101620)
                                        )
                                    )

                                    OutlinedTextField(
                                        value = targetPackage,
                                        onValueChange = { targetPackage = it },
                                        label = { Text("Android Package Name", fontFamily = FontFamily.Monospace) },
                                        modifier = Modifier.weight(0.6f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = accentColor,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF131A24),
                                            unfocusedContainerColor = Color(0xFF101620)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick Popular Apps Shortcuts
                                Text(
                                    text = "1-Tap Presets:",
                                    color = Color(0xFF8899A6),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val quickApps = listOf(
                                        Triple("Calculator", "com.google.android.calculator", "Calculator"),
                                        Triple("Google", "com.google.android.googlequicksearchbox", "Google"),
                                        Triple("Chrome", "com.android.chrome", "Chrome"),
                                        Triple("CS 1.6 Client", "in.celest.xash3d.cs16client", "CS 1.6")
                                    )
                                    quickApps.forEach { (name, pkg, label) ->
                                        Box(
                                            modifier = Modifier
                                                .clip(CutCornerShape(4.dp))
                                                .background(if (targetPackage == pkg) accentColor else Color(0xFF1E2836))
                                                .clickable {
                                                    targetAppName = name
                                                    targetPackage = pkg
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (targetPackage == pkg) Color.Black else Color.White,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Search Installed Device Apps
                                OutlinedTextField(
                                    value = appSearchQuery,
                                    onValueChange = { appSearchQuery = it },
                                    placeholder = { Text("Filter installed apps on device...", color = Color(0xFF64748B), fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentColor,
                                        unfocusedBorderColor = Color(0xFF2C394B),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0C1016),
                                        unfocusedContainerColor = Color(0xFF0C1016)
                                    )
                                )

                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                        .background(Color(0xFF0A0D12))
                                ) {
                                    items(filteredApps) { app ->
                                        val isSelected = targetPackage == app.packageName
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    targetPackage = app.packageName
                                                    targetAppName = app.appName
                                                }
                                                .background(if (isSelected) Color(0xFF1E2B3C) else Color.Transparent)
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = app.appName,
                                                    color = if (isSelected) accentColor else Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Text(
                                                    text = app.packageName,
                                                    color = Color(0xFF64748B),
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }

                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = accentColor)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141C26))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            onResetDefaults()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("RESET DEFAULTS", color = Color(0xFFEF4444), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    Row {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("CANCEL", color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = { applyAndClose() },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = CutCornerShape(4.dp)
                        ) {
                            Text("SAVE & APPLY", color = Color.Black, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        if (showAppPickerDialog) {
            AppPickerDialog(
                currentTargetPackage = targetPackage,
                accentColor = accentColor,
                onAppSelected = { app ->
                    targetPackage = app.packageName
                    targetAppName = app.appName
                },
                onDismiss = {
                    showAppPickerDialog = false
                }
            )
        }
    }
}
