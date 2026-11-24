# FocusLock Android APK Automated Build Script
# 100% self-contained: uses portable JDK 17, Android SDK tools, and Gradle 8.5

$ErrorActionPreference = "Stop"
$toolsDir = "$PSScriptRoot\.build-tools"
$jdkDir = "$toolsDir\jdk-17"
$androidHome = "$toolsDir\android-sdk"
$gradleDir = "$toolsDir\gradle-8.5"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " FocusLock Automated APK Builder" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

if (!(Test-Path $toolsDir)) {
    New-Item -ItemType Directory -Path $toolsDir -Force | Out-Null
}

# 1. OpenJDK 17 (Already present)
if (!(Test-Path "$jdkDir\bin\javac.exe")) {
    Write-Host "[1/5] Downloading OpenJDK 17 (Portable)..." -ForegroundColor Yellow
    $jdkZip = "$toolsDir\openjdk-17.zip"
    $jdkUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.10_7.zip"
    & curl.exe -L -o $jdkZip $jdkUrl
    Expand-Archive -Path $jdkZip -DestinationPath $toolsDir -Force
    $extractedFolder = Get-ChildItem -Path $toolsDir -Directory -Filter "jdk-17*" | Select-Object -First 1
    Move-Item -Path $extractedFolder.FullName -Destination $jdkDir -Force
    Remove-Item $jdkZip -Force
} else {
    Write-Host "[1/5] OpenJDK 17 is ready." -ForegroundColor Green
}

# 2. Gradle 8.5 (Already present)
if (!(Test-Path "$gradleDir\bin\gradle.bat")) {
    Write-Host "[2/5] Downloading Gradle 8.5 (Portable)..." -ForegroundColor Yellow
    $gradleZip = "$toolsDir\gradle-8.5-bin.zip"
    $gradleUrl = "https://services.gradle.org/distributions/gradle-8.5-bin.zip"
    & curl.exe -L -o $gradleZip $gradleUrl
    Expand-Archive -Path $gradleZip -DestinationPath $toolsDir -Force
    Remove-Item $gradleZip -Force
} else {
    Write-Host "[2/5] Gradle 8.5 is ready." -ForegroundColor Green
}

# 3. Android Command Line Tools
if (!(Test-Path "$androidHome\cmdline-tools\latest\bin\sdkmanager.bat")) {
    Write-Host "[3/5] Downloading Android Commandline Tools with curl..." -ForegroundColor Yellow
    $cmdlineZip = "$toolsDir\cmdline-tools.zip"
    if (Test-Path $cmdlineZip) { Remove-Item $cmdlineZip -Force }
    
    $cmdlineUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
    & curl.exe -L -k --retry 3 -o $cmdlineZip $cmdlineUrl

    Write-Host "Extracting Android Commandline Tools..." -ForegroundColor Yellow
    $tempExtract = "$toolsDir\cmdline_temp"
    if (Test-Path $tempExtract) { Remove-Item $tempExtract -Recurse -Force }
    Expand-Archive -Path $cmdlineZip -DestinationPath $tempExtract -Force
    
    New-Item -ItemType Directory -Path "$androidHome\cmdline-tools\latest" -Force | Out-Null
    Copy-Item -Path "$tempExtract\cmdline-tools\*" -Destination "$androidHome\cmdline-tools\latest" -Recurse -Force
    Remove-Item $cmdlineZip -Force
    Remove-Item $tempExtract -Recurse -Force
} else {
    Write-Host "[3/5] Android Commandline Tools are ready." -ForegroundColor Green
}

# 4. Set Environment Variables
$env:JAVA_HOME = $jdkDir
$env:ANDROID_HOME = $androidHome
$env:PATH = "$jdkDir\bin;$gradleDir\bin;$androidHome\cmdline-tools\latest\bin;$androidHome\platform-tools;$env:PATH"

# 5. Accept Licenses & Install Android SDK Platform 34
Write-Host "[4/5] Installing Android Platform 34 and Build Tools..." -ForegroundColor Yellow
$sdkManager = "$androidHome\cmdline-tools\latest\bin\sdkmanager.bat"

$licensesDir = "$androidHome\licenses"
New-Item -ItemType Directory -Path $licensesDir -Force | Out-Null
Set-Content -Path "$licensesDir\android-sdk-license" -Value "`n24333f8a63b6825ea9c5514f83c2829b004d1fee"

& $sdkManager --sdk_root=$androidHome "platforms;android-34" "build-tools;34.0.0" "platform-tools"

# 6. Run Gradle to compile the APK
Write-Host "[5/5] Compiling FocusLock APK..." -ForegroundColor Green
$projectDir = "$PSScriptRoot\android-dumbphone-launcher"
Set-Location -Path $projectDir

Set-Content -Path "$projectDir\local.properties" -Value "sdk.dir=$($androidHome -replace '\\', '/')"

& "$gradleDir\bin\gradle.bat" assembleDebug

$outputApk = "$projectDir\app\build\outputs\apk\debug\app-debug.apk"
$tempApk = Join-Path $env:TEMP "focuslock-build\app\outputs\apk\debug\app-debug.apk"
if (!(Test-Path $outputApk) -and (Test-Path $tempApk)) {
    $outputApk = $tempApk
}
if (Test-Path $outputApk) {
    $finalApk = "$PSScriptRoot\FocusLock-Liquid.apk"
    Copy-Item -Path $outputApk -Destination $finalApk -Force
    Write-Host "`n=======================================================" -ForegroundColor Green
    Write-Host "🎉 SUCCESS! Your APK is ready at:" -ForegroundColor Green
    Write-Host "   $finalApk" -ForegroundColor Cyan
    Write-Host "=======================================================" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ Compilation finished. Please check app/build/outputs/apk/debug/ for app-debug.apk" -ForegroundColor Yellow
}
