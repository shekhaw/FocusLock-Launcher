package com.dumbphone.launcher.ui.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dumbphone.launcher.data.*
import com.dumbphone.launcher.ui.theme.WallpaperTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val prefs = PreferencesManager(application)

    private val _screenState = MutableStateFlow(ScreenState.HOME)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _allInstalledApps = MutableStateFlow<List<AppItem>>(emptyList())
    val allInstalledApps: StateFlow<List<AppItem>> = _allInstalledApps.asStateFlow()

    private val _homeApps = MutableStateFlow<List<AppItem?>>(List(6) { null })
    val homeApps: StateFlow<List<AppItem?>> = _homeApps.asStateFlow()

    private val _gateType = MutableStateFlow(UnlockGateType.PHRASE)
    val gateType: StateFlow<UnlockGateType> = _gateType.asStateFlow()

    private val _customPhrase = MutableStateFlow("")
    val customPhrase: StateFlow<String> = _customPhrase.asStateFlow()

    private val _enteredPhrase = MutableStateFlow("")
    val enteredPhrase: StateFlow<String> = _enteredPhrase.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _targetPin = MutableStateFlow("1234")
    val targetPin: StateFlow<String> = _targetPin.asStateFlow()

    private val _wallpaperTheme = MutableStateFlow(WallpaperTheme.AURORA)
    val wallpaperTheme: StateFlow<WallpaperTheme> = _wallpaperTheme.asStateFlow()

    private val _hasCustomWallpaper = MutableStateFlow(false)
    val hasCustomWallpaper: StateFlow<Boolean> = _hasCustomWallpaper.asStateFlow()

    private val _wallpaperUpdateTrigger = MutableStateFlow(0L)
    val wallpaperUpdateTrigger: StateFlow<Long> = _wallpaperUpdateTrigger.asStateFlow()

    private val _sessionSecondsRemaining = MutableStateFlow(120)
    val sessionSecondsRemaining: StateFlow<Int> = _sessionSecondsRemaining.asStateFlow()

    // Control Center & Quick Settings state
    private val _isControlCenterOpen = MutableStateFlow(false)
    val isControlCenterOpen: StateFlow<Boolean> = _isControlCenterOpen.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isWifiEnabled = MutableStateFlow(true)
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _isSilentMode = MutableStateFlow(false)
    val isSilentMode: StateFlow<Boolean> = _isSilentMode.asStateFlow()

    private val _volumePercent = MutableStateFlow(0.65f)
    val volumePercent: StateFlow<Float> = _volumePercent.asStateFlow()

    private val _brightnessPercent = MutableStateFlow(0.75f)
    val brightnessPercent: StateFlow<Float> = _brightnessPercent.asStateFlow()

    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    // Desktop glass widgets (0: Weather, 1: Battery & System, 2: Mindful Focus)
    private val _desktopWidgetType = MutableStateFlow(0)
    val desktopWidgetType: StateFlow<Int> = _desktopWidgetType.asStateFlow()

    // Liquid Glass Tuning Studio
    private val _glassRefractionOffset = MutableStateFlow(70f)
    val glassRefractionOffset: StateFlow<Float> = _glassRefractionOffset.asStateFlow()

    private val _glassDispersion = MutableStateFlow(0.55f)
    val glassDispersion: StateFlow<Float> = _glassDispersion.asStateFlow()

    private val _glassTintTone = MutableStateFlow("CLEAR")
    val glassTintTone: StateFlow<String> = _glassTintTone.asStateFlow()

    private val _glassTintAlpha = MutableStateFlow(0.0f)
    val glassTintAlpha: StateFlow<Float> = _glassTintAlpha.asStateFlow()

    private var sessionTimerJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _gateType.value = prefs.getGateType()
            _customPhrase.value = prefs.getCustomPhrase()
            _targetPin.value = prefs.getPinCode()
            _wallpaperTheme.value = prefs.getWallpaperTheme()
            _hasCustomWallpaper.value = prefs.isCustomWallpaperEnabled()
            _wallpaperUpdateTrigger.value = prefs.getCustomWallpaperTimestamp()

            _glassRefractionOffset.value = prefs.getGlassRefractionOffset()
            _glassDispersion.value = prefs.getGlassDispersion()
            _glassTintTone.value = prefs.getGlassTintTone()
            _glassTintAlpha.value = prefs.getGlassTintAlpha()

            val installed = repository.getInstalledApps()
            _allInstalledApps.value = installed

            val savedPackages = prefs.getHomeAppPackages()
            val mapped = List(6) { index ->
                if (index < savedPackages.size) {
                    val pkg = savedPackages[index]
                    installed.find { it.packageName == pkg }
                } else null
            }
            _homeApps.value = mapped
        }
    }

    fun navigateTo(screen: ScreenState) {
        if (screen == ScreenState.HOME) {
            stopSessionTimer()
            resetFrictionInputs()
        }
        _screenState.value = screen
    }

    fun openFrictionGate() {
        resetFrictionInputs()
        _screenState.value = ScreenState.GATE
    }

    fun setEnteredPhrase(text: String) {
        _enteredPhrase.value = text
    }

    fun isPhraseMatching(): Boolean {
        val typed = _enteredPhrase.value.trim().lowercase()
        val target = _customPhrase.value.trim().lowercase()
        return typed.isNotEmpty() && typed == target
    }

    fun onPinDigit(digit: Char) {
        if (_enteredPin.value.length < 4) {
            _enteredPin.value += digit
            if (_enteredPin.value.length == 4) {
                verifyPin()
            }
        }
    }

    fun clearPin() {
        _enteredPin.value = ""
    }

    fun backspacePin() {
        if (_enteredPin.value.isNotEmpty()) {
            _enteredPin.value = _enteredPin.value.dropLast(1)
        }
    }

    private fun verifyPin() {
        if (_enteredPin.value == _targetPin.value) {
            unlockVault()
        } else {
            clearPin()
        }
    }

    fun unlockVault() {
        _screenState.value = ScreenState.DRAWER
        startSessionTimer()
    }

    private fun resetFrictionInputs() {
        _enteredPhrase.value = ""
        _enteredPin.value = ""
    }

    private fun startSessionTimer() {
        sessionTimerJob?.cancel()
        _sessionSecondsRemaining.value = 120
        sessionTimerJob = viewModelScope.launch {
            while (_sessionSecondsRemaining.value > 0) {
                delay(1000)
                _sessionSecondsRemaining.value -= 1
            }
            navigateTo(ScreenState.HOME)
        }
    }

    fun stopSessionTimer() {
        sessionTimerJob?.cancel()
        sessionTimerJob = null
    }

    fun launchApp(packageName: String) {
        val success = repository.launchApp(packageName)
        if (success) {
            navigateTo(ScreenState.HOME)
        }
    }

    fun updateHomeSlot(slotIndex: Int, packageName: String?) {
        val currentPackages = _homeApps.value.map { it?.packageName }.toMutableList()
        while (currentPackages.size < 6) currentPackages.add(null)

        if (packageName == null || packageName.isBlank()) {
            currentPackages[slotIndex] = null
        } else {
            currentPackages[slotIndex] = packageName
        }

        val nonNullClean = currentPackages.filterNotNull()
        prefs.saveHomeAppPackages(nonNullClean)
        loadData()
    }

    fun setGateMode(type: UnlockGateType) {
        prefs.setGateType(type)
        _gateType.value = type
    }

    fun updateCustomPhrase(phrase: String) {
        prefs.setCustomPhrase(phrase)
        _customPhrase.value = phrase
    }

    fun updatePin(pin: String) {
        prefs.setPinCode(pin)
        _targetPin.value = pin
    }

    fun setWallpaperTheme(theme: WallpaperTheme) {
        prefs.setWallpaperTheme(theme)
        _wallpaperTheme.value = theme
        _hasCustomWallpaper.value = false
        _wallpaperUpdateTrigger.value = System.currentTimeMillis()
    }

    fun setCustomPhotoWallpaper(uri: Uri) {
        viewModelScope.launch {
            val success = prefs.saveCustomWallpaperFromUri(getApplication(), uri)
            if (success) {
                _hasCustomWallpaper.value = true
                _wallpaperUpdateTrigger.value = System.currentTimeMillis()
            }
        }
    }

    fun clearCustomWallpaper() {
        viewModelScope.launch {
            prefs.clearCustomWallpaper(getApplication())
            _hasCustomWallpaper.value = false
            _wallpaperUpdateTrigger.value = System.currentTimeMillis()
        }
    }

    fun getCustomWallpaperFile(): File {
        return prefs.getCustomWallpaperFile(getApplication())
    }

    // Control Center Management
    fun openControlCenter() {
        refreshSystemControls()
        _isControlCenterOpen.value = true
    }

    fun closeControlCenter() {
        _isControlCenterOpen.value = false
    }

    fun toggleControlCenter() {
        if (!_isControlCenterOpen.value) {
            refreshSystemControls()
        }
        _isControlCenterOpen.value = !_isControlCenterOpen.value
    }

    fun refreshSystemControls() {
        val ctx = getApplication<Application>()
        try {
            val wifi = ctx.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            _isWifiEnabled.value = wifi?.isWifiEnabled == true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val bt = BluetoothAdapter.getDefaultAdapter()
            _isBluetoothEnabled.value = bt?.isEnabled == true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val audio = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            _isSilentMode.value = (audio.ringerMode == AudioManager.RINGER_MODE_SILENT || audio.ringerMode == AudioManager.RINGER_MODE_VIBRATE)

            val maxVol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val curVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (maxVol > 0) {
                _volumePercent.value = (curVol.toFloat() / maxVol.toFloat()).coerceIn(0f, 1f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleTorch() {
        val ctx = getApplication<Application>()
        val newState = !_isTorchOn.value
        try {
            val cameraManager = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (hasFlash) {
                    cameraManager.setTorchMode(id, newState)
                    _isTorchOn.value = newState
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isTorchOn.value = newState
        }
    }

    fun toggleWifi(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                @Suppress("DEPRECATION")
                wifi?.let {
                    val next = !it.isWifiEnabled
                    it.isWifiEnabled = next
                    _isWifiEnabled.value = next
                }
            }
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun toggleBluetooth(context: Context) {
        try {
            val bt = BluetoothAdapter.getDefaultAdapter()
            if (bt != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                if (bt.isEnabled) {
                    bt.disable()
                    _isBluetoothEnabled.value = false
                } else {
                    bt.enable()
                    _isBluetoothEnabled.value = true
                }
            } else {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun toggleSilence(context: Context) {
        try {
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audio.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                audio.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                _isSilentMode.value = true
            } else {
                audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
                _isSilentMode.value = false
            }
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun setVolumePercent(percent: Float) {
        val clamped = percent.coerceIn(0f, 1f)
        _volumePercent.value = clamped
        try {
            val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (maxVol * clamped).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBrightnessPercent(percent: Float) {
        _brightnessPercent.value = percent.coerceIn(0.02f, 1f)
    }

    fun toggleMusic() {
        try {
            val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            val eventUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(eventDown)
            audioManager.dispatchMediaKeyEvent(eventUp)
            _isMusicPlaying.value = !_isMusicPlaying.value
        } catch (e: Exception) {
            e.printStackTrace()
            _isMusicPlaying.value = !_isMusicPlaying.value
        }
    }

    fun cycleDesktopWidget() {
        _desktopWidgetType.value = (_desktopWidgetType.value + 1) % 3
    }

    // Liquid Glass Studio Customization
    fun setGlassRefractionOffset(value: Float) {
        _glassRefractionOffset.value = value
        prefs.setGlassRefractionOffset(value)
    }

    fun setGlassDispersion(value: Float) {
        _glassDispersion.value = value
        prefs.setGlassDispersion(value)
    }

    fun setGlassTintTone(tone: String, alpha: Float) {
        _glassTintTone.value = tone
        _glassTintAlpha.value = alpha
        prefs.setGlassTintTone(tone)
        prefs.setGlassTintAlpha(alpha)
    }

    fun resetGlassToDefault() {
        setGlassRefractionOffset(70f)
        setGlassDispersion(0.55f)
        setGlassTintTone("CLEAR", 0.0f)
    }
}
