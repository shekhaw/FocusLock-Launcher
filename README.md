# FocusLock - Minimal Dumb Phone Launcher for Android 📱

A distraction-free, OLED-black Android launcher engineered to eliminate mindless phone addiction and doomscrolling.

---

## 🌟 Core Features

1. **6 Essential Apps Only**
   - The home screen strictly displays up to 6 apps chosen by you (e.g. Phone, Messages, Maps, Notes, Camera, WhatsApp).
   - Zero notification badges, zero colorful attention-grabbing app icons, zero infinite feeds.

2. **The "Importance Gate" (Anti-Doomscroll Friction)**
   - All other apps are completely hidden and locked behind an intentional barrier.
   - When you tap **"Access Other Apps"**, it asks: *"Is this important right now?"*
   - To unlock the rest of your apps, you must pass one of two friction gates:
     - **Mindful Phrase Barrier**: You must accurately type a sentence you set (e.g., *"I am opening my phone for a real purpose"*). Typing a full sentence activates your prefrontal cortex, shattering autopilot scrolling habits.
     - **Phone PIN Passcode**: Enter a custom 4-to-6 digit PIN.

3. **Auto-Relocking Vault**
   - Once unlocked, you get a 2-minute temporary window to use whatever tool you needed.
   - As soon as you launch an app or return home, the vault automatically locks itself again.

4. **Pure OLED Aesthetic**
   - Deep `#000000` background saves battery and drastically lowers eye strain and dopamine stimulation.
   - Minimalist digital clock and date display.

---

## 📂 Project Structure

```text
Apps dev/
├── preview/
│   └── index.html                <-- Interactive Phone Simulator (Try in your browser right now!)
│
└── android-dumbphone-launcher/   <-- Full Native Android Studio Project (Kotlin + Jetpack Compose)
    ├── settings.gradle.kts
    ├── build.gradle.kts
    ├── gradle.properties
    └── app/
        ├── build.gradle.kts
        └── src/main/
            ├── AndroidManifest.xml   <-- Includes HOME and DEFAULT intent filters
            ├── java/com/dumbphone/launcher/
            │   ├── MainActivity.kt
            │   ├── LauncherApp.kt
            │   ├── data/
            │   │   ├── AppModel.kt
            │   │   ├── PreferencesManager.kt
            │   │   └── AppRepository.kt
            │   └── ui/
            │       ├── theme/ (Color, Theme, Type)
            │       ├── viewmodel/LauncherViewModel.kt
            │       └── screens/
            │           ├── HomeScreen.kt
            │           ├── FrictionGateDialog.kt
            │           ├── AppDrawerScreen.kt
            │           └── SettingsScreen.kt
            └── res/
```

---

## 🚀 How to Try the Interactive Simulator Right Now

Open `Apps dev/preview/index.html` in your browser (Google Chrome, Edge, etc.):
1. Double-click [index.html](file:///c:/Users/Shekhawat/OneDrive/Documents/antigravity/Apps%20dev/preview/index.html).
2. You will see an interactive Android phone mockup.
3. Tap on the 6 slots, tap "Access Other Apps", try typing the intentional phrase or using the PIN pad to unlock the hidden apps.
4. Go to settings to customize the phrase and reassign your 6 favorite apps.

---

## 🔨 How to Build the APK in Android Studio

1. Download and install [Android Studio](https://developer.android.com/studio).
2. Open Android Studio and select **Open**.
3. Select the folder: `c:\Users\Shekhawat\OneDrive\Documents\antigravity\Apps dev\android-dumbphone-launcher`.
4. Wait for Gradle to sync dependencies.
5. Connect your Android phone via USB (with USB Debugging enabled) or use the Android Studio Emulator.
6. Click the green **Run (▶)** button.
7. On your phone, press the Home button and choose **FocusLock** as your **Always** home app.
