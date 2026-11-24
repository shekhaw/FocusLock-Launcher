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
import com.dumbphone.launcher.ui.components.liquidGlassRefraction
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
                        .liquidGlassRefraction(
                            cornerRadiusPx = 72f,
                            refractionHeight = 28f,
                            refractionAmount = 20f,
                            chromaticAberration = 0.6f,
                            depthEffect = 0.25f,
                            blurRadius = 10f
                        )
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.09f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.2.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.85f),
                                        GlassChromaticCyan,
                                        Color.White.copy(alpha = 0.30f),
                                        GlassChromaticMagenta,
                                        Color.White.copy(alpha = 0.12f)
                                    )
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { viewModel.openFrictionGate() }
                        .padding(horizontal = 26.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Access Other Apps",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            style = Typography.bodyMedium.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = GlassTextRefractionGlow,
                                    offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                                    blurRadius = 2.5f
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real Physical macOS Dock Glass Tray:
 * Implements physical glass optics:
 * 1. Caustic Light Banding: directional luminous light streaks refracted through the glass slab.
 * 2. Specular Fresnel Horizon: intense top arc glare catching overhead ceiling light.
 * 3. Optical Depth Trough: inner refraction shadow line simulating 6mm glass thickness.
 * 4. Chromatic Dispersion Rim: razor-sharp specular border with subtle cyan/magenta prism split.
 * 5. Refracted Typography & 3D Glass Pip: crystalline submerged text styling.
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
                elevation = 24.dp,
                shape = dockShape,
                ambientColor = Color.Black.copy(alpha = 0.65f),
                spotColor = Color.White.copy(alpha = 0.25f)
            )
            .liquidGlassRefraction(
                cornerRadiusPx = 84f,
                refractionHeight = 48f,
                refractionAmount = 32f,
                chromaticAberration = 0.85f,
                depthEffect = 0.32f,
                blurRadius = 14f
            )
            .clip(dockShape)
            // Layer 1: Translucent Glass Medium
            .background(
                Brush.verticalGradient(
                    listOf(
                        MacDockGlassTop,
                        MacDockGlassBottom
                    )
                )
            )
            // Layer 2: Chromatic Dispersion Specular Rim
            .border(
                BorderStroke(
                    1.4.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            MacDockBorderTop,
                            GlassChromaticCyan,
                            MacDockBorderMid,
                            GlassChromaticMagenta,
                            MacDockBorderBottom
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(800f, 1200f)
                    )
                ),
                shape = dockShape
            )
    ) {
        // Layer 3: Caustic Light Banding (Diagonal Luminous Sheen Bands)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GlassCausticTrough,
                            GlassCausticMid,
                            GlassCausticPeak,
                            GlassCausticTrough,
                            GlassCausticPeak.copy(alpha = 0.36f),
                            GlassCausticMid,
                            GlassCausticTrough
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
        )

        // Layer 4: Top Specular Glare Arc (Curved Glass Horizon)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.42f),
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Layer 5: Inner Refractive Trough Shadow (Physical Glass Slab Thickness)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.2.dp, start = 2.dp, end = 2.dp)
                .height(3.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GlassInnerBevelShadow,
                            Color.Transparent
                        )
                    )
                )
        )

        // Layer 6: Inside Rows with Refracted Typography
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            homeApps.forEachIndexed { index, app ->
                MacDockAppRow(
                    slotIndex = index,
                    app = app,
                    onClick = { onSlotClick(index, app) }
                )
                if (index < homeApps.lastIndex) {
                    // Dual-tone groove divider (specular bevel + shadow trough)
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        HorizontalDivider(
                            color = MacDockDivider,
                            thickness = 0.75.dp
                        )
                        HorizontalDivider(
                            color = MacDockDividerShadow,
                            thickness = 0.75.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single Row inside the macOS Dock Tray with Submerged Refracted Typography
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
            // 3D Luminous Glass Pip Indicator
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isAssigned) {
                            Brush.radialGradient(
                                listOf(
                                    Color.White,
                                    GlassChromaticCyan,
                                    Color.White.copy(alpha = 0.6f)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        }
                    )
                    .border(
                        0.5.dp,
                        if (isAssigned) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(50)
                    )
            )
            Spacer(modifier = Modifier.width(14.dp))
            if (app != null) {
                Text(
                    text = app.label,
                    style = Typography.bodyLarge.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = GlassTextRefractionGlow,
                            offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                            blurRadius = 3f
                        )
                    ),
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
                color = Color.White.copy(alpha = 0.55f),
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
