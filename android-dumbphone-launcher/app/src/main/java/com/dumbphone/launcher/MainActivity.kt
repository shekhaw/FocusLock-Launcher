package com.dumbphone.launcher

import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.ui.components.AliveWallpaper
import com.dumbphone.launcher.ui.components.LocalWallpaperContainer
import com.dumbphone.launcher.ui.screens.AppDrawerScreen
import com.dumbphone.launcher.ui.screens.FrictionGateScreen
import com.dumbphone.launcher.ui.screens.HomeScreen
import com.dumbphone.launcher.ui.screens.SettingsScreen
import com.dumbphone.launcher.ui.theme.DumbPhoneTheme
import com.dumbphone.launcher.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Official modern Android edge-to-edge
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Enable complete display cutout bleed & immersive full-screen
        setupImmersiveFullscreen()

        setContent {
            DumbPhoneTheme {
                val screenState by viewModel.screenState.collectAsState()
                val wallpaperTheme by viewModel.wallpaperTheme.collectAsState()
                val hasCustomWallpaper by viewModel.hasCustomWallpaper.collectAsState()
                val wallpaperUpdateTrigger by viewModel.wallpaperUpdateTrigger.collectAsState()

                // Intercept back button: Always navigate back to Home and re-lock
                BackHandler(enabled = (screenState != ScreenState.HOME)) {
                    viewModel.navigateTo(ScreenState.HOME)
                }

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

                var wallpaperContainer by remember { mutableStateOf<ViewGroup?>(null) }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Continuous Wallpaper Layer: Hosted in FrameLayout as sampling source for LiquidGlass
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                val composeView = ComposeView(ctx).apply {
                                    setContent {
                                        if (customWallpaperBitmap != null) {
                                            Image(
                                                bitmap = customWallpaperBitmap.asImageBitmap(),
                                                contentDescription = "Wallpaper",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.30f))
                                            )
                                        } else {
                                            AliveWallpaper(theme = wallpaperTheme)
                                        }
                                    }
                                }
                                addView(
                                    composeView,
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT
                                )
                                wallpaperContainer = this
                            }
                        },
                        update = { frameLayout ->
                            val composeView = frameLayout.getChildAt(0) as? ComposeView
                            composeView?.setContent {
                                if (customWallpaperBitmap != null) {
                                    Image(
                                        bitmap = customWallpaperBitmap.asImageBitmap(),
                                        contentDescription = "Wallpaper",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.30f))
                                    )
                                } else {
                                    AliveWallpaper(theme = wallpaperTheme)
                                }
                            }
                        }
                    )

                    // Active screen rendered in transparent surface over the shared wallpaper
                    CompositionLocalProvider(LocalWallpaperContainer provides wallpaperContainer) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent
                        ) {
                            when (screenState) {
                                ScreenState.HOME -> HomeScreen(viewModel = viewModel)
                                ScreenState.GATE -> FrictionGateScreen(viewModel = viewModel)
                                ScreenState.DRAWER -> AppDrawerScreen(viewModel = viewModel)
                                ScreenState.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupImmersiveFullscreen()
        viewModel.loadData()
    }

    private fun setupImmersiveFullscreen() {
        // 1. Tell Window to layout with NO limits (bleeds behind camera cutout, notch, and navigation pill)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // 2. Cutout mode: Bleed under camera notch/punch-hole
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // 3. Fully transparent system bars
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        // 4. InsetsController: hide status bar until swiped down
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
