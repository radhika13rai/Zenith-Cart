#!/usr/bin/env bash
# ==============================================================================
# OmniCart: Standalone Apps Exporter & Git Branch Generator
# ==============================================================================
# This script extracts and generates 3 completely decoupled standalone Android
# projects with dedicated application IDs, launcher activities, and icons:
#
# 1. OmniCart Shopper (Customer App)
#    - ID: com.omnicart.shopper
#    - Entry: CustomerAppActivity
#    - Features: Grocery catalog, itemized 5% GST cart, 10-min live map tracker
#
# 2. OmniCart Partner (Merchant App)
#    - ID: com.omnicart.partner
#    - Entry: PartnerAppActivity
#    - Features: 90s pick & pack queue, inventory stock editing, darkstores, bank settlements
#
# 3. OmniCart Rider (Delivery Fleet App)
#    - ID: com.omnicart.rider
#    - Entry: RiderAppActivity
#    - Features: Duty toggle, darkstore pod dispatches, OTP handovers, rider wallet
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
TARGET_DIR="${1:-${ROOT_DIR}/standalone_exports}"

echo "=================================================================="
echo "🚀 OmniCart Standalone Apps Exporter"
echo "📂 Project Source: ${ROOT_DIR}"
echo "📦 Export Target:  ${TARGET_DIR}"
echo "=================================================================="

mkdir -p "${TARGET_DIR}"

export_app() {
    local APP_KEY="$1"
    local APP_NAME="$2"
    local APP_ID="$3"
    local LAUNCHER_ACTIVITY="$4"
    local APP_ICON="$5"
    local APP_ROUND_ICON="$6"
    local OUT_DIR="${TARGET_DIR}/OmniCart-${APP_KEY}"

    echo ""
    echo "------------------------------------------------------------------"
    echo "🔨 Exporting ${APP_NAME} to: ${OUT_DIR}"
    echo "   Application ID: ${APP_ID}"
    echo "   Launcher:       ${LAUNCHER_ACTIVITY}"
    echo "------------------------------------------------------------------"

    rm -rf "${OUT_DIR}"
    mkdir -p "${OUT_DIR}"

    # 1. Copy common build structure
    cp "${ROOT_DIR}/build.gradle.kts" "${OUT_DIR}/"
    cp "${ROOT_DIR}/settings.gradle.kts" "${OUT_DIR}/"
    cp "${ROOT_DIR}/gradle.properties" "${OUT_DIR}/"
    cp "${ROOT_DIR}/.env.example" "${OUT_DIR}/" 2>/dev/null || true
    if [ -f "${ROOT_DIR}/.env" ]; then
        cp "${ROOT_DIR}/.env" "${OUT_DIR}/" 2>/dev/null || true
    fi
    if [ -f "${ROOT_DIR}/.gitignore" ]; then
        cp "${ROOT_DIR}/.gitignore" "${OUT_DIR}/"
    fi
    cp -r "${ROOT_DIR}/gradle" "${OUT_DIR}/"

    # 2. Copy app folder structure
    mkdir -p "${OUT_DIR}/app"
    cp -r "${ROOT_DIR}/app/src" "${OUT_DIR}/app/"
    if [ -f "${ROOT_DIR}/app/proguard-rules.pro" ]; then
        cp "${ROOT_DIR}/app/proguard-rules.pro" "${OUT_DIR}/app/"
    fi

    # Remove multi-flavor source sets in standalone export
    rm -rf "${OUT_DIR}/app/src/shopper" "${OUT_DIR}/app/src/partner" "${OUT_DIR}/app/src/rider" "${OUT_DIR}/app/src/unified"

    # 3. Create standalone build.gradle.kts without multi-flavors
    cat << 'EOF' > "${OUT_DIR}/app/build.gradle.kts"
import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
EOF

    echo "    applicationId = \"${APP_ID}\"" >> "${OUT_DIR}/app/build.gradle.kts"
    cat << 'EOF' >> "${OUT_DIR}/app/build.gradle.kts"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.maps)
  implementation(libs.maps.compose)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
EOF

    # 4. Generate standalone AndroidManifest.xml
    cat << EOF > "${OUT_DIR}/app/src/main/AndroidManifest.xml"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="${APP_ICON}"
        android:label="@string/app_name"
        android:roundIcon="${APP_ROUND_ICON}"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">

        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="\${MAPS_API_KEY}" />

        <activity
            android:name=".${LAUNCHER_ACTIVITY}"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
EOF

    # 5. Set strings.xml app_name
    cat << EOF > "${OUT_DIR}/app/src/main/res/values/strings.xml"
<resources>
    <string name="app_name">${APP_NAME}</string>
</resources>
EOF

    # 6. Standalone README.md
    cat << EOF > "${OUT_DIR}/README.md"
# ${APP_NAME}

Dedicated Standalone Application for OmniCart Quick-Commerce Ecosystem.

- **Package / Application ID:** \`${APP_ID}\`
- **Main Launcher Activity:** \`com.example.${LAUNCHER_ACTIVITY}\`
- **Architecture:** Jetpack Compose + Kotlin Coroutines + Room DB + Material Design 3

## Build and Run

To build the debug APK:
\`\`\`bash
./gradlew assembleDebug
\`\`\`
The APK will be generated at \`app/build/outputs/apk/debug/app-debug.apk\`.

To install directly to an Android device:
\`\`\`bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
\`\`\`
EOF

    echo "✅ Exported ${APP_NAME} successfully to ${OUT_DIR}"
}

# Export the 3 individual standalone apps
export_app "Shopper" "OmniCart Shopper" "com.omnicart.shopper" "CustomerAppActivity" "@mipmap/ic_launcher" "@mipmap/ic_launcher_round"
export_app "Partner" "OmniCart Partner" "com.omnicart.partner" "PartnerAppActivity" "@mipmap/ic_launcher_partner" "@mipmap/ic_launcher_partner_round"
export_app "Rider" "OmniCart Rider" "com.omnicart.rider" "RiderAppActivity" "@mipmap/ic_launcher_rider" "@mipmap/ic_launcher_rider_round"

echo ""
echo "=================================================================="
echo "🎉 SUCCESS: All 3 Standalone Apps Exported!"
echo "📁 Shopper: ${TARGET_DIR}/OmniCart-Shopper"
echo "📁 Partner: ${TARGET_DIR}/OmniCart-Partner"
echo "📁 Rider:   ${TARGET_DIR}/OmniCart-Rider"
echo "=================================================================="
