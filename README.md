# OmniCart Rider

Dedicated Standalone Application for OmniCart Quick-Commerce Ecosystem.

- **Package / Application ID:** `com.omnicart.rider`
- **Main Launcher Activity:** `com.example.RiderAppActivity`
- **Architecture:** Jetpack Compose + Kotlin Coroutines + Room DB + Material Design 3

## Build and Run

### 1. Production Signed Release APK (Play Store / Direct Install)
```bash
./gradlew assembleRelease
```
The signed production APK will be generated at:
```
app/build/outputs/apk/release/app-release.apk
```

### 2. Google Play Store App Bundle (.aab)
```bash
./gradlew bundleRelease
```
The signed App Bundle for Google Play Console submission will be generated at:
```
app/build/outputs/bundle/release/app-release.aab
```

### 3. Debug APK (Development & Testing)
```bash
./gradlew assembleDebug
```
The debug APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 4. Install via ADB
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```
