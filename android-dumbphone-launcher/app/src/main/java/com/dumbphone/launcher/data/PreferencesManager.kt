package com.dumbphone.launcher.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.dumbphone.launcher.ui.theme.WallpaperTheme
import java.io.File
import java.io.FileOutputStream

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("dumb_phone_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOME_APPS = "key_home_apps"
        private const val KEY_GATE_TYPE = "key_gate_type"
        private const val KEY_CUSTOM_PHRASE = "key_custom_phrase"
        private const val KEY_PIN_CODE = "key_pin_code"
        private const val KEY_WALLPAPER = "key_wallpaper_theme"
        private const val KEY_CUSTOM_WALLPAPER_ENABLED = "key_custom_wallpaper_enabled"
        private const val KEY_CUSTOM_WALLPAPER_TIMESTAMP = "key_custom_wallpaper_timestamp"
        private const val CUSTOM_WALLPAPER_FILENAME = "custom_wallpaper.jpg"
        private const val DEFAULT_PHRASE = "I am opening my phone for a real purpose."
        private const val DEFAULT_PIN = "1234"
        private const val KEY_GLASS_REFRACTION_OFFSET = "key_glass_refraction_offset"
        private const val KEY_GLASS_DISPERSION = "key_glass_dispersion"
        private const val KEY_GLASS_TINT_TONE = "key_glass_tint_tone"
        private const val KEY_GLASS_TINT_ALPHA = "key_glass_tint_alpha"
    }

    fun getHomeAppPackages(): List<String> {
        val raw = prefs.getString(KEY_HOME_APPS, null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(",").filter { it.isNotBlank() }.take(6)
    }

    fun saveHomeAppPackages(packages: List<String>) {
        val limited = packages.take(6)
        prefs.edit().putString(KEY_HOME_APPS, limited.joinToString(",")).apply()
    }

    fun getGateType(): UnlockGateType {
        val raw = prefs.getString(KEY_GATE_TYPE, UnlockGateType.PHRASE.name)
        return try {
            UnlockGateType.valueOf(raw ?: UnlockGateType.PHRASE.name)
        } catch (e: Exception) {
            UnlockGateType.PHRASE
        }
    }

    fun setGateType(type: UnlockGateType) {
        prefs.edit().putString(KEY_GATE_TYPE, type.name).apply()
    }

    fun getCustomPhrase(): String {
        return prefs.getString(KEY_CUSTOM_PHRASE, DEFAULT_PHRASE) ?: DEFAULT_PHRASE
    }

    fun setCustomPhrase(phrase: String) {
        val clean = phrase.trim()
        if (clean.isNotEmpty()) {
            prefs.edit().putString(KEY_CUSTOM_PHRASE, clean).apply()
        }
    }

    fun getPinCode(): String {
        return prefs.getString(KEY_PIN_CODE, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setPinCode(pin: String) {
        if (pin.length in 4..6 && pin.all { it.isDigit() }) {
            prefs.edit().putString(KEY_PIN_CODE, pin).apply()
        }
    }

    fun getWallpaperTheme(): WallpaperTheme {
        val raw = prefs.getString(KEY_WALLPAPER, WallpaperTheme.AURORA.name)
        return try {
            WallpaperTheme.valueOf(raw ?: WallpaperTheme.AURORA.name)
        } catch (e: Exception) {
            WallpaperTheme.AURORA
        }
    }

    fun setWallpaperTheme(theme: WallpaperTheme) {
        prefs.edit()
            .putString(KEY_WALLPAPER, theme.name)
            .putBoolean(KEY_CUSTOM_WALLPAPER_ENABLED, false)
            .apply()
    }

    fun isCustomWallpaperEnabled(): Boolean {
        return prefs.getBoolean(KEY_CUSTOM_WALLPAPER_ENABLED, false)
    }

    fun setCustomWallpaperEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CUSTOM_WALLPAPER_ENABLED, enabled).apply()
    }

    fun getCustomWallpaperTimestamp(): Long {
        return prefs.getLong(KEY_CUSTOM_WALLPAPER_TIMESTAMP, 0L)
    }

    fun getCustomWallpaperFile(context: Context): File {
        return File(context.filesDir, CUSTOM_WALLPAPER_FILENAME)
    }

    fun saveCustomWallpaperFromUri(context: Context, uri: Uri): Boolean {
        return try {
            val destFile = getCustomWallpaperFile(context)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            prefs.edit()
                .putBoolean(KEY_CUSTOM_WALLPAPER_ENABLED, true)
                .putLong(KEY_CUSTOM_WALLPAPER_TIMESTAMP, System.currentTimeMillis())
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clearCustomWallpaper(context: Context) {
        try {
            val file = getCustomWallpaperFile(context)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        prefs.edit()
            .putBoolean(KEY_CUSTOM_WALLPAPER_ENABLED, false)
            .putLong(KEY_CUSTOM_WALLPAPER_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getGlassRefractionOffset(): Float = prefs.getFloat(KEY_GLASS_REFRACTION_OFFSET, 70f)
    fun setGlassRefractionOffset(value: Float) {
        prefs.edit().putFloat(KEY_GLASS_REFRACTION_OFFSET, value).apply()
    }

    fun getGlassDispersion(): Float = prefs.getFloat(KEY_GLASS_DISPERSION, 0.55f)
    fun setGlassDispersion(value: Float) {
        prefs.edit().putFloat(KEY_GLASS_DISPERSION, value).apply()
    }

    fun getGlassTintTone(): String = prefs.getString(KEY_GLASS_TINT_TONE, "CLEAR") ?: "CLEAR"
    fun setGlassTintTone(tone: String) {
        prefs.edit().putString(KEY_GLASS_TINT_TONE, tone).apply()
    }

    fun getGlassTintAlpha(): Float = prefs.getFloat(KEY_GLASS_TINT_ALPHA, 0.0f)
    fun setGlassTintAlpha(alpha: Float) {
        prefs.edit().putFloat(KEY_GLASS_TINT_ALPHA, alpha).apply()
    }
}
