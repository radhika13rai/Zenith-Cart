import subprocess
import shutil
import re
import os

ROOT_DIR = "/app/applet"
GRADLE_FILE = os.path.join(ROOT_DIR, "app/build.gradle.kts")
MANIFEST_FILE = os.path.join(ROOT_DIR, "app/src/main/AndroidManifest.xml")

with open(GRADLE_FILE, "r") as f:
    orig_gradle = f.read()

with open(MANIFEST_FILE, "r") as f:
    orig_manifest = f.read()

apps = [
    {
        "name": "Shopper",
        "apk_name": "OmniCart_Shopper.apk",
        "app_id": "com.omnicart.shopper",
        "launcher": "CustomerAppActivity"
    },
    {
        "name": "Partner",
        "apk_name": "OmniCart_Partner.apk",
        "app_id": "com.omnicart.partner",
        "launcher": "PartnerAppActivity"
    },
    {
        "name": "Rider",
        "apk_name": "OmniCart_Rider.apk",
        "app_id": "com.omnicart.rider",
        "launcher": "RiderAppActivity"
    }
]

try:
    for app in apps:
        print(f"==========================================")
        print(f"🔨 Building Standalone APK: {app['apk_name']} ({app['app_id']})")
        print(f"==========================================")

        # 1. Update Gradle applicationId
        new_gradle = re.sub(
            r'applicationId\s*=\s*"[^"]+"',
            f'applicationId = "{app["app_id"]}"',
            orig_gradle
        )
        with open(GRADLE_FILE, "w") as f:
            f.write(new_gradle)

        # 2. Update Manifest: ensure only this app's activity has LAUNCHER intent-filter
        # Read clean manifest
        manifest_text = orig_manifest
        
        # If building Partner, ensure PartnerAppActivity is the main launcher
        # If building Rider, ensure RiderAppActivity is the main launcher
        # If building Shopper, ensure CustomerAppActivity is the main launcher
        
        # Let's adjust launcher filters
        # First remove launcher from all
        manifest_no_launchers = re.sub(
            r'<intent-filter>\s*<action android:name="android.intent.action.MAIN"\s*/>\s*<category android:name="android.intent.category.LAUNCHER"\s*/>\s*</intent-filter>',
            '',
            manifest_text
        )
        
        # Add launcher specifically to target activity
        target_activity_regex = rf'(<activity\s+android:name="\.{app["launcher"]}"[^>]*>)'
        replacement = r'\1\n            <intent-filter>\n                <action android:name="android.intent.action.MAIN" />\n                <category android:name="android.intent.category.LAUNCHER" />\n            </intent-filter>'
        updated_manifest = re.sub(target_activity_regex, replacement, manifest_no_launchers)

        with open(MANIFEST_FILE, "w") as f:
            f.write(updated_manifest)

        # 3. Run Gradle assembleDebug
        print(f"⚙️ Running gradle :app:assembleDebug...")
        res = subprocess.run(["gradle", ":app:assembleDebug"], cwd=ROOT_DIR, capture_output=True, text=True)
        if res.returncode != 0:
            print(f"❌ Failed to build {app['apk_name']}:\n{res.stderr}\n{res.stdout}")
            break
        
        # 4. Copy generated APK
        source_apk = os.path.join(ROOT_DIR, "app/build/outputs/apk/debug/app-debug.apk")
        if not os.path.exists(source_apk):
            # Check .build-outputs
            source_apk = os.path.join(ROOT_DIR, ".build-outputs/app-debug.apk")

        dest_apk = os.path.join(ROOT_DIR, app["apk_name"])
        shutil.copy2(source_apk, dest_apk)
        print(f"✅ Generated {app['apk_name']} ({os.path.getsize(dest_apk) // (1024 * 1024)} MB)")

finally:
    # Always restore original files
    print("🔄 Restoring original configuration...")
    with open(GRADLE_FILE, "w") as f:
        f.write(orig_gradle)
    with open(MANIFEST_FILE, "w") as f:
        f.write(orig_manifest)
    print("🎉 Done!")
