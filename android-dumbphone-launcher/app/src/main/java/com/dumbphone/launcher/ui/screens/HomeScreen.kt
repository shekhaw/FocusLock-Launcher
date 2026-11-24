package com.dumbphone.launcher.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.AppItem
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.ui.components.GlassTextShadow
import com.dumbphone.launcher.ui.components.MacDockGlassCard
import com.dumbphone.launcher.ui.components.MacDockGlassDivider
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
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Floating Clock (Pure typography floating on wallpaper, high contrast shadow)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
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

            // 2. The 6 Home Apps: Genuine Translucent macOS Dock Glass Shelf
            MacDockGlassTray(
                homeApps = homeApps,
                onSlotClick = { _, app ->
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
                MacDockGlassPill(
                    modifier = Modifier
                        .clickable { viewModel.openFrictionGate() }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
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
                            style = Typography.bodyMedium.copy(shadow = GlassTextShadow)
                        )
                    }
                }
            }
        }
    }
}

/**
 * macOS Dock Glass Tray
 */
@Composable
fun MacDockGlassTray(
    homeApps: List<AppItem?>,
    onSlotClick: (Int, AppItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    MacDockGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 28.dp
    ) {
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
                    MacDockGlassDivider(paddingHorizontal = 16.dp)
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
            // 3D Glass Pip Indicator
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isAssigned) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.25f)
                    )
            )
            Spacer(modifier = Modifier.width(14.dp))
            if (app != null) {
                Text(
                    text = app.label,
                    style = Typography.bodyLarge.copy(shadow = GlassTextShadow),
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
            } else {
                Text(
                    text = "+ Add Slot ${slotIndex + 1}",
                    style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                    color = Color.White.copy(alpha = 0.45f)
                )
            }
        }

        if (app != null) {
            Text(
                text = "›",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.65f),
                fontWeight = FontWeight.Light,
                style = Typography.bodyMedium.copy(shadow = GlassTextShadow)
            )
        } else {
            Text(
                text = "+",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.35f),
                style = Typography.bodyMedium.copy(shadow = GlassTextShadow)
            )
        }
    }
}
