package com.dumbphone.launcher.ui.viewmodel

import android.app.Application
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

    private val _sessionSecondsRemaining = MutableStateFlow(120)
    val sessionSecondsRemaining: StateFlow<Int> = _sessionSecondsRemaining.asStateFlow()

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
    }
}
