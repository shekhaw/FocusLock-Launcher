package com.dumbphone.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.ui.components.AppIconImage
import com.dumbphone.launcher.ui.components.GlassTextShadow
import com.dumbphone.launcher.ui.components.MacDockGlassCard
import com.dumbphone.launcher.ui.components.MacDockGlassDivider
import com.dumbphone.launcher.ui.components.MacDockGlassPill
import com.dumbphone.launcher.ui.theme.*
import com.dumbphone.launcher.ui.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val allApps by viewModel.allInstalledApps.collectAsState()
    val secondsLeft by viewModel.sessionSecondsRemaining.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery, allApps) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter { it.label.contains(searchQuery, ignoreCase = true) }
        }
    }

    val minutes = secondsLeft / 60
    val seconds = String.format("%02d", secondsLeft % 60)

    // Transparent container: shared continuous wallpaper from MainActivity is visible underneath!
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Card (macOS Glass Card)
            MacDockGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "All Applications",
                            style = Typography.titleMedium.copy(shadow = GlassTextShadow),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Vault Unlocked ($minutes:$seconds remaining)",
                            style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                            color = Color(0xFFFDE047)
                        )
                    }

                    MacDockGlassPill(
                        modifier = Modifier.clickable { viewModel.navigateTo(ScreenState.HOME) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Relock 🔒",
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                        )
                    }
                }
            }

            // 2. Search Bar (Apple Glass Capsule Pill)
            MacDockGlassPill(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search installed apps...",
                                color = Color.White.copy(alpha = 0.50f),
                                fontSize = 14.5.sp
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            containerColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. Installed Apps List inside macOS Glass Shelf
            MacDockGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cornerRadius = 24.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp)
                ) {
                    items(filteredApps, key = { it.packageName + it.activityName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.launchApp(app.packageName) }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Authentic Native Android Application Icon (Apple Squircle)
                                AppIconImage(
                                    drawable = app.icon,
                                    label = app.label,
                                    modifier = Modifier.size(46.dp),
                                    cornerRadius = 11.5.dp
                                )

                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = app.label,
                                        style = Typography.bodyLarge.copy(shadow = GlassTextShadow),
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                                        color = Color.White.copy(alpha = 0.45f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = "Open ›",
                                style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                                color = Color.White.copy(alpha = 0.60f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        MacDockGlassDivider(paddingHorizontal = 16.dp)
                    }
                }
            }
        }
    }
}
