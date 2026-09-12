package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.LauncherConfig
import java.io.File

@Composable
fun LoginScreen(
    config: LauncherConfig,
    onLoginSuccess: () -> Unit,
    onOpenSettings: () -> Unit,
    onUpdateTargetApp: (packageName: String, appName: String) -> Unit = { _, _ -> }
) {
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val accentColor = config.themeColor.getColor()
    val cfOrange = Color(0xFFFFA000)

    fun attemptLogin() {
        val trimmedUser = usernameInput.trim()
        val trimmedPass = passwordInput

        // Validate credentials offline against stored config
        val isUserRegistered = trimmedUser.equals(config.registeredUsername.trim(), ignoreCase = true)
        val isPasswordCorrect = trimmedPass == config.registeredPassword

        if (!isUserRegistered || !isPasswordCorrect) {
            errorMessage = "Wrong password"
            return
        }

        errorMessage = null
        onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D14))
    ) {
        // --- LOGIN SCREEN BACKGROUND (CUSTOM PICTURE OR PRESET) ---
        if (config.customLoginBgPath != null && File(config.customLoginBgPath).exists()) {
            AsyncImage(
                model = File(config.customLoginBgPath),
                contentDescription = "Login Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val resId = when (config.loginBgPreset) {
                "neon" -> R.drawable.img_loading_bg_1789191723589
                else -> R.drawable.img_cs_dust_banner_1789190469975
            }
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Login Background Preset",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Dark subtle overlay to keep wallpaper visible while ensuring UI legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x33000000),
                            Color(0x7705080E),
                            Color(0xAA020408)
                        )
                    )
                )
        )

        // --- PRETEND ONLINE STATUS (100% OFFLINE, ZERO NETWORK REQUIRED) ---
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp, start = 8.dp)
                .background(Color(0xCC090E17), CutCornerShape(3.dp))
                .border(1.dp, Color(0xFF1E293B), CutCornerShape(3.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SERVER: CF-ASIA #01 [ONLINE]  PING: 18ms",
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- SETTINGS BUTTON (MOVED FAR TO THE CORNER) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp, end = 4.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                shape = CutCornerShape(3.dp),
                color = Color(0xCC0D141F),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor),
                modifier = Modifier.size(34.dp)
            ) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // --- CROSSFIRE STYLE COMPACT LOGIN BOX (MOVED DOWNWARDS) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .width(260.dp)
                    .clip(CutCornerShape(6.dp))
                    .border(1.5.dp, accentColor, CutCornerShape(6.dp)),
                color = Color(0xF00A0F17)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Crossfire Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF141D2A), CutCornerShape(3.dp))
                            .border(1.dp, Color(0xFF26354A), CutCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "CROSSFIRE",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LOGIN",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // USERNAME / ID FIELD
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = {
                            usernameInput = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                "ID / Username",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("username_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFF283648),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF101722),
                            unfocusedContainerColor = Color(0xFF0C1118)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // PASSWORD FIELD WITH EYE TOGGLE
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                "Password",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = if (passwordVisible) accentColor else Color(0xFF718096),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFF283648),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF101722),
                            unfocusedContainerColor = Color(0xFF0C1118)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { attemptLogin() })
                    )

                    // Error message
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(CutCornerShape(2.dp))
                                .background(Color(0x33EF4444))
                                .border(1.dp, Color(0xFFEF4444), CutCornerShape(2.dp))
                                .padding(vertical = 2.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFFF6B6B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // CROSSFIRE "GAME START" / "LOGIN" BUTTON
                    Button(
                        onClick = { attemptLogin() },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = CutCornerShape(3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("login_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GAME START",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
