package com.dumbphone.launcher.data

import android.content.Context
import android.content.SharedPreferences
import com.dumbphone.launcher.ui.theme.WallpaperTheme

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("dumb_phone_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOME_APPS = "key_home_apps"
        private const val KEY_GATE_TYPE = "key_gate_type"
        private const val KEY_CUSTOM_PHRASE = "key_custom_phrase"
        private const val KEY_PIN_CODE = "key_pin_code"
        private const val KEY_WALLPAPER = "key_wallpaper_theme"
        private const val DEFAULT_PHRASE = "I am opening my phone for a real purpose."
        private const val DEFAULT_PIN = "1234"
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
        prefs.edit().putString(KEY_WALLPAPER, theme.name).apply()
    }
}
