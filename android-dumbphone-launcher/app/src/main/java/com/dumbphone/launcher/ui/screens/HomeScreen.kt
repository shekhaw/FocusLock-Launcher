package com.dumbphone.launcher.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.AppItem
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.ui.components.AliveWallpaper
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
    val wallpaperTheme by viewModel.wallpaperTheme.collectAsState()

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
        // Living, breathing animated fluid wallpaper canvas
        AliveWallpaper(theme = wallpaperTheme)

        // Inner Content Container with custom safe padding for punch-hole and gesture pill
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 54.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Floating Clock (NO glass card, clean typography floating on wallpaper)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
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
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "“Simplicity is the ultimate sophistication.”",
                    style = Typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            // 2. The 6 Home Apps Slots (Apple Liquid Glass Pills)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                homeApps.forEachIndexed { index, app ->
                    LiquidGlassAppPill(
                        slotIndex = index,
                        app = app,
                        onAppClick = {
                            if (app != null) {
                                viewModel.launchApp(app.packageName)
                            } else {
                                viewModel.navigateTo(ScreenState.SETTINGS)
                            }
                        }
                    )
                }
            }

            // 3. Bottom: Access Other Apps Pill
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassWhiteSubtle)
                        .border(1.dp, GlassWhiteBorder, RoundedCornerShape(24.dp))
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
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Access Other Apps",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidGlassAppPill(
    slotIndex: Int,
    app: AppItem?,
    onAppClick: () -> Unit
) {
    val isAssigned = (app != null)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isAssigned) GlassWhiteSubtle else GlassWhiteVerySubtle)
            .border(
                1.dp,
                if (isAssigned) GlassWhiteBorder else GlassWhiteBorder.copy(alpha = 0.15f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onAppClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app != null) {
                Text(
                    text = app.label,
                    style = Typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "→",
                    style = Typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.4f)
                )
            } else {
                Text(
                    text = "+ Add Slot ${slotIndex + 1}",
                    style = Typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.35f)
                )
                Text(
                    text = "+",
                    style = Typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.35f)
                )
            }
        }
    }
}
