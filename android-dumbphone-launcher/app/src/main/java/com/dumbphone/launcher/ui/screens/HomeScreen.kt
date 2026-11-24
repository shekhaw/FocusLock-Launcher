package com.dumbphone.launcher.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.AppItem
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.ui.components.AliveWallpaper
import com.dumbphone.launcher.ui.theme.*
import com.dumbphone.launcher.ui.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val homeApps by viewModel.homeApps.collectAsState()
    val wallpaperTheme by viewModel.wallpaperTheme.collectAsState()
    val hasCustomWallpaper by viewModel.hasCustomWallpaper.collectAsState()
    val wallpaperUpdateTrigger by viewModel.wallpaperUpdateTrigger.collectAsState()

    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    // Load custom photo bitmap if enabled
    val customWallpaperBitmap = remember(hasCustomWallpaper, wallpaperUpdateTrigger) {
        if (hasCustomWallpaper) {
            val file = viewModel.getCustomWallpaperFile()
            if (file.exists() && file.length() > 0) {
                try {
                    BitmapFactory.decodeFile(file.absolutePath)
                } catch (e: Exception) {
                    null
                }
            } else null
        } else null
    }

    // Outer Edge-to-Edge Container: Wallpaper bleeds 100% full screen from top bezel to bottom bezel
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        viewModel.navigateTo(ScreenState.SETTINGS)
                    }
                )
            }
    ) {
        // Wallpaper Layer: Custom User Photo OR Alive Living Fluid Shader
        if (customWallpaperBitmap != null) {
            Image(
                bitmap = customWallpaperBitmap.asImageBitmap(),
                contentDescription = "Custom Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Ambient legibility scrim to ensure glass dock and typography remain crystalline
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            )
        } else {
            AliveWallpaper(theme = wallpaperTheme)
        }

        // Inner Content Container with custom safe padding for punch-hole and gesture pill
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, end = 22.dp, top = 54.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Floating Clock (Pure typography floating directly on wallpaper, no card)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentTime,
                    style = Typography.displayLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentDate,
                    style = Typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "“Simplicity is the ultimate sophistication.”",
                    style = Typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }

            // 2. The 6 Home Apps: Unified macOS Dock Glass Shelf / Tray
            MacDockGlassTray(
                homeApps = homeApps,
                onSlotClick = { index, app ->
                    if (app != null) {
                        viewModel.launchApp(app.packageName)
                    } else {
                        viewModel.navigateTo(ScreenState.SETTINGS)
                    }
                }
            )

            // 3. Bottom: Access Other Apps (macOS Control Center Capsule Pill)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.18f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.50f),
                                        Color.White.copy(alpha = 0.15f)
                                    )
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { viewModel.openFrictionGate() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Access Other Apps",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * macOS Dock Glass Tray:
 * Floating translucent squircle container with specular rim highlight reflection,
 * subtle inner depth, and hairline glass item dividers.
 */
@Composable
fun MacDockGlassTray(
    homeApps: List<AppItem?>,
    onSlotClick: (Int, AppItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    val dockShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = dockShape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(dockShape)
            // macOS Frosted Glass Floor
            .background(
                Brush.verticalGradient(
                    listOf(
                        MacDockGlassTop,
                        MacDockGlassBottom
                    )
                )
            )
            // macOS Dual-Rim Specular Edge Reflection
            .border(
                BorderStroke(
                    1.25.dp,
                    Brush.verticalGradient(
                        listOf(
                            MacDockBorderTop,
                            MacDockBorderMid,
                            MacDockBorderBottom
                        )
                    )
                ),
                shape = dockShape
            )
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            homeApps.forEachIndexed { index, app ->
                MacDockAppRow(
                    slotIndex = index,
                    app = app,
                    onClick = { onSlotClick(index, app) }
                )
                if (index < homeApps.lastIndex) {
                    HorizontalDivider(
                        color = MacDockDivider,
                        thickness = 0.75.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Single Row inside the macOS Dock Tray
 */
@Composable
fun MacDockAppRow(
    slotIndex: Int,
    app: AppItem?,
    onClick: () -> Unit
) {
    val isAssigned = (app != null)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subtle glass bullet indicator
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isAssigned) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f)
                    )
            )
            Spacer(modifier = Modifier.width(14.dp))
            if (app != null) {
                Text(
                    text = app.label,
                    style = Typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
            } else {
                Text(
                    text = "+ Add Slot ${slotIndex + 1}",
                    style = Typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.35f)
                )
            }
        }

        if (app != null) {
            Text(
                text = "›",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.45f),
                fontWeight = FontWeight.Light
            )
        } else {
            Text(
                text = "+",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.30f)
            )
        }
    }
}
