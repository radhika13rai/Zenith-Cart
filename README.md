# OmniCart Partner

Dedicated Standalone Application for OmniCart Quick-Commerce Ecosystem.

- **Package / Application ID:** `com.omnicart.partner`
- **Main Launcher Activity:** `com.example.PartnerAppActivity`
- **Architecture:** Jetpack Compose + Kotlin Coroutines + Room DB + Material Design 3

## Build and Run

To build the debug APK:
```bash
./gradlew assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

To install directly to an Android device:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
