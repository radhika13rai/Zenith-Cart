# OmniCart: 3 Standalone Apps Architecture & Deployment Guide

OmniCart is a quick-commerce ecosystem featuring **3 dedicated, independently deployable applications**:

| App Name | Audience | Package / Application ID | Main Launcher | Icon |
| :--- | :--- | :--- | :--- | :--- |
| **🛒 OmniCart Shopper** | Shoppers & Consumers | `com.omnicart.shopper` | [`CustomerAppActivity`](file:///root/Zenith-Cart/app/src/main/java/com/example/CustomerAppActivity.kt) | `ic_launcher` |
| **🏪 OmniCart Partner** | Darkstore Merchants & Vendors | `com.omnicart.partner` | [`PartnerAppActivity`](file:///root/Zenith-Cart/app/src/main/java/com/example/PartnerAppActivity.kt) | `ic_launcher_partner` |
| **🛵 OmniCart Rider** | Delivery Fleet & Couriers | `com.omnicart.rider` | [`RiderAppActivity`](file:///root/Zenith-Cart/app/src/main/java/com/example/RiderAppActivity.kt) | `ic_launcher_rider` |
| **📱 OmniCart Unified** | Debug & Full Demo Suite | `com.aistudio.omnicart.xkpw` | [`MainActivity`](file:///root/Zenith-Cart/app/src/main/java/com/example/MainActivity.kt) | `ic_launcher` |

---

## 🏗️ Architectural Clean Separation

Each app is decoupled at the UI, activity, manifest, and build flavor levels:

1. **Dedicated Composable Trees**:
   - `CustomerAppScreen(viewModel)`: 10-minute grocery catalogue, 5% GST & packaging cart, live order tracking, location picker, wishlist, customer support.
   - `PartnerAppScreen(viewModel)`: 90-second inbound order pick & pack queue, live product addition/stock editor, micro-darkstore inventory, bank & UPI payout analytics.
   - `RiderFleetScreen(viewModel)`: On/off duty toggle, 10-minute flash dispatch pod requests, 4-digit doorstep handover OTP verification, rider earnings wallet.

2. **Dedicated Launcher Activities**:
   - `CustomerAppActivity`: Directly boots into `CustomerAppScreen`.
   - `PartnerAppActivity`: Directly boots into `PartnerAppScreen`.
   - `RiderAppActivity`: Directly boots into `RiderFleetScreen`.
   - `MainActivity`: Orchestrates all 3 with an ecosystem switcher pill bar for internal testing.

3. **Manifest Source Sets (No Duplicate Icon Conflicts)**:
   - `app/src/main/AndroidManifest.xml`: Declares activities without launcher intent filters.
   - `app/src/shopper/AndroidManifest.xml`: Attaches `LAUNCHER` to `CustomerAppActivity`.
   - `app/src/partner/AndroidManifest.xml`: Attaches `LAUNCHER` to `PartnerAppActivity`.
   - `app/src/rider/AndroidManifest.xml`: Attaches `LAUNCHER` to `RiderAppActivity`.
   - `app/src/unified/AndroidManifest.xml`: Attaches `LAUNCHER` to `MainActivity`.

---

## 🚀 Building Standalone APKs via Gradle Flavors

Run standard Gradle assemble commands from the project root:

```bash
# 1. Build OmniCart Shopper APK (com.omnicart.shopper)
./gradlew :app:assembleShopperDebug

# 2. Build OmniCart Partner APK (com.omnicart.partner)
./gradlew :app:assemblePartnerDebug

# 3. Build OmniCart Rider APK (com.omnicart.rider)
./gradlew :app:assembleRiderDebug

# 4. Build Unified Multi-Role APK (com.aistudio.omnicart.xkpw)
./gradlew :app:assembleUnifiedDebug
```

Output APK locations:
- `app/build/outputs/apk/shopper/debug/app-shopper-debug.apk`
- `app/build/outputs/apk/partner/debug/app-partner-debug.apk`
- `app/build/outputs/apk/rider/debug/app-rider-debug.apk`
- `app/build/outputs/apk/unified/debug/app-unified-debug.apk`

---

## 📦 Automated Export to 3 Independent Projects / Repositories

To export 3 distinct standalone project folders ready for separate GitHub repositories:

```bash
# Run the standalone exporter
./scripts/export_standalone_apps.sh /path/to/destination
```

This generates:
- `/path/to/destination/OmniCart-Shopper`
- `/path/to/destination/OmniCart-Partner`
- `/path/to/destination/OmniCart-Rider`

Each directory contains a standalone `app/build.gradle.kts`, standalone `AndroidManifest.xml`, standalone `README.md`, and complete Kotlin source trees with zero flavor dependencies.
