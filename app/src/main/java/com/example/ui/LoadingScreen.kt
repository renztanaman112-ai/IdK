package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.LauncherConfig
import java.io.File

@Composable
fun LoadingScreen(
    config: LauncherConfig,
    onLoadingComplete: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    val durationMs = (config.loadingDurationSeconds.coerceAtLeast(1) * 1000)
    val accentColor = config.themeColor.getColor()

    val statusText by remember {
        derivedStateOf {
            val p = progress.value
            when {
                p < 0.25f -> "CONNECTING TO MASTER SERVER [18ms]..."
                p < 0.55f -> "VERIFYING SECURITY CERTIFICATES..."
                p < 0.85f -> "SYNCING GAME PATCH v2.44.1..."
                p < 0.98f -> "INITIALIZING TACTICAL ENGINE..."
                else -> "SERVER CONNECTED - WELCOME"
            }
        }
    }

    LaunchedEffect(durationMs) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
        )
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E14))
    ) {
        // Loading Background (Custom Picture or Preset)
        if (config.customLoadingBgPath != null && File(config.customLoadingBgPath).exists()) {
            AsyncImage(
                model = File(config.customLoadingBgPath),
                contentDescription = "Loading Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val resId = when (config.loadingBgPreset) {
                "dust" -> R.drawable.img_cs_dust_banner_1789190469975
                else -> R.drawable.img_loading_bg_1789191723589
            }
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Loading Screen Preset",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Dark gradient overlay for bottom legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x22000000),
                            Color(0x66000000),
                            Color(0xDD070B10)
                        )
                    )
                )
        )

        // Pretend Online Server Status (100% Offline behind the scenes)
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

        // LOADING BAR POSITIONED DOWN AT THE BOTTOM (CIRCLE REMOVED)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status row above bar
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .widthIn(max = 560.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${(progress.value * 100).toInt()}%",
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // Tactical Loading Bar down at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .widthIn(max = 560.dp)
                    .height(10.dp)
                    .clip(CutCornerShape(2.dp))
                    .background(Color(0x77111822))
                    .border(1.dp, Color(0xFF334155), CutCornerShape(2.dp))
                    .testTag("loading_progress_bar")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(10.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.8f),
                                    accentColor
                                )
                            )
                        )
                )
            }
        }
    }
}
