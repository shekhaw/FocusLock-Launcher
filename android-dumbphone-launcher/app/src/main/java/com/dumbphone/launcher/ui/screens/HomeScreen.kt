package com.dumbphone.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.AppItem
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.ui.components.GlassTextShadow
import com.dumbphone.launcher.ui.components.MacDockGlassCard
import com.dumbphone.launcher.ui.components.MacDockGlassPill
import com.dumbphone.launcher.ui.theme.*
import com.dumbphone.launcher.ui.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val homeApps by viewModel.homeApps.collectAsState()

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

    // Outer Edge-to-Edge Container (Transparent; continuous wallpaper is rendered in MainActivity)
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
        // Safe inner padding for status bar and gesture navigation bar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Floating Clock & Date (Minimalist typography floating directly on wallpaper)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentTime,
                    style = Typography.displayLarge.copy(shadow = GlassTextShadow),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentDate,
                    style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                    color = Color.White.copy(alpha = 0.90f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "“Simplicity is the ultimate sophistication.”",
                    style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                    color = Color.White.copy(alpha = 0.65f)
                )
            }

            // 2. Bottom Shelf Section: Authentic Horizontal macOS Dock
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Access Other Apps (Floating macOS Capsule Pill directly above dock)
                MacDockGlassPill(
                    modifier = Modifier
                        .clickable { viewModel.openFrictionGate() }
                        .padding(horizontal = 20.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Access Other Apps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            style = Typography.bodyMedium.copy(shadow = GlassTextShadow)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Authentic macOS Horizontal Dock Shelf
                MacDockHorizontalShelf(
                    homeApps = homeApps,
                    onSlotClick = { index, app ->
                        if (app != null) {
                            viewModel.launchApp(app.packageName)
                        } else {
                            viewModel.navigateTo(ScreenState.SETTINGS)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Authentic Horizontal macOS Dock Shelf:
 * Sleek bottom glass shelf magnifying the wallpaper via AndroidLiquidGlassView,
 * with horizontal app slots, glass squircles, and crystalline text labels.
 */
@Composable
fun MacDockHorizontalShelf(
    homeApps: List<AppItem?>,
    onSlotClick: (Int, AppItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    MacDockGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp),
        cornerRadius = 26.dp,
        refractionHeight = 20.dp,
        refractionOffset = 70.dp,
        dispersion = 0.55f
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            homeApps.forEachIndexed { index, app ->
                MacDockAppSlot(
                    slotIndex = index,
                    app = app,
                    onClick = { onSlotClick(index, app) }
                )
            }
        }
    }
}

/**
 * Single App Slot inside the macOS Horizontal Dock Shelf
 */
@Composable
fun MacDockAppSlot(
    slotIndex: Int,
    app: AppItem?,
    onClick: () -> Unit
) {
    val isAssigned = (app != null)

    Column(
        modifier = Modifier
            .width(50.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Squircle Icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (isAssigned) {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.12f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.03f)
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            if (isAssigned) Color.White.copy(alpha = 0.70f) else Color.White.copy(alpha = 0.25f),
                            if (isAssigned) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f)
                        )
                    ),
                    shape = RoundedCornerShape(11.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (app != null) {
                // Styled Glyph / Initial of the App
                val initial = if (app.label.isNotBlank()) app.label.take(1).uppercase() else "•"
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = Typography.titleMedium.copy(shadow = GlassTextShadow)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Slot",
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label below squircle
        Text(
            text = app?.label ?: "Add",
            fontSize = 10.sp,
            color = if (isAssigned) Color.White else Color.White.copy(alpha = 0.50f),
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = Typography.labelSmall.copy(shadow = GlassTextShadow),
            modifier = Modifier.fillMaxWidth()
        )

        // 3D Glass Pip indicator dot under assigned app
        if (isAssigned) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.90f))
            )
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}
