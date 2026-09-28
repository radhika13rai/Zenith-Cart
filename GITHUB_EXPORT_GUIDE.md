# OmniCart: 3 Separate Apps GitHub Deployment Guide

This guide details how to publish and deploy the **3 separate OmniCart apps** to GitHub and Google Play Store:

| App Name | Target Audience | Standalone Launcher Activity | Default Application ID |
| :--- | :--- | :--- | :--- |
| **1. OmniCart Shopper** | Customers / Buyers | `com.example.CustomerAppActivity` | `com.aistudio.omnicart.shopper` |
| **2. OmniCart Partner** | Darkstore Merchants & Vendors | `com.example.PartnerAppActivity` | `com.aistudio.omnicart.partner` |
| **3. OmniCart Rider** | Delivery Fleet & Riders | `com.example.RiderAppActivity` | `com.aistudio.omnicart.rider` |

---

## Method 1: Push from AI Studio UI (Instant Sync)

1. Look at the **top right corner or Project Settings** in Google AI Studio.
2. Click **"Push to GitHub"** (or **"Export as ZIP"**).
3. If this is your first time, authorize AI Studio to link with your GitHub account.
4. Select or create your destination repository (e.g., `omnicart-ecosystem`).
5. AI Studio will automatically push all source files, Gradle scripts, database entities, and the 3 standalone app entry points.

---

## Method 2: Push as 3 Separate GitHub Repositories

If you want 3 distinct GitHub repositories on your profile:
* `https://github.com/your-username/omnicart-shopper`
* `https://github.com/your-username/omnicart-partner`
* `https://github.com/your-username/omnicart-rider`

Run the following commands in your terminal:

### Step 1: Add Remotes for the 3 Repositories
```bash
git remote add origin-shopper https://github.com/YOUR_USERNAME/omnicart-shopper.git
git remote add origin-partner https://github.com/YOUR_USERNAME/omnicart-partner.git
git remote add origin-rider   https://github.com/YOUR_USERNAME/omnicart-rider.git
```

### Step 2: Push App 1 (Shopper App)
To make `CustomerAppActivity` the primary launch activity in `AndroidManifest.xml`:
```bash
git checkout -b app-shopper
# In AndroidManifest.xml, ensure CustomerAppActivity has android.intent.category.LAUNCHER
git push -u origin-shopper app-shopper:main
```

### Step 3: Push App 2 (Merchant Partner App)
```bash
git checkout -b app-partner
# In AndroidManifest.xml, ensure PartnerAppActivity has android.intent.category.LAUNCHER
git push -u origin-partner app-partner:main
```

### Step 4: Push App 3 (Delivery Fleet / Rider App)
```bash
git checkout -b app-rider
# In AndroidManifest.xml, ensure RiderAppActivity has android.intent.category.LAUNCHER
git push -u origin-rider app-rider:main
```

---

## Method 3: Unified App with Standalone Mode (`lockMode`)

The codebase is built with modular flexibility:
* **Interactive Mode (`lockMode = false`):** The top pill switcher allows testing and demonstrating all three apps in a single installed APK.
* **Locked Production Mode (`lockMode = true`):** Removes the switcher completely, locking the user into their respective role (Shopper, Merchant, or Delivery Boy) for production Play Store release.

```kotlin
// Example inside CustomerAppActivity:
OmniMainScreen(
    viewModel = viewModel,
    initialMode = EcosystemApp.CUSTOMER,
    lockMode = true // Top switcher removed; strictly customer app
)
```
