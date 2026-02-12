# EPS Viewer - Quick Start Guide

## 🚀 5-Minute Setup

### Step 1: Open Project
```bash
# In Android Studio: File → Open → Select /testeps folder
```

### Step 2: Sync Gradle
```
Android Studio will automatically sync Gradle files
Wait for indexing to complete
```

### Step 3: Build Debug APK
```bash
./gradlew assembleDebug
# or use Android Studio: Build → Build Bundle(s)/APK(s) → Build APK(s)
```

### Step 4: Install on Device/Emulator
```bash
./gradlew installDebug
```

### Step 5: Configure for Production (Before Play Store)
1. **Update Billing IDs**
   - File: `app/src/main/java/com/example/epsviewer/data/repository/BillingRepositoryImpl.kt`
   - Replace: Product IDs with your Play Console IDs

2. **Add Ad Unit ID**
   - File: `app/src/main/AndroidManifest.xml`
   - Replace: `ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy` with your AdMob ID

3. **Change Package Name**
   - File: `app/build.gradle.kts`
   - Update: `namespace` and `applicationId`

4. **Integrate EPS Renderer**
   - File: `app/src/main/java/com/example/epsviewer/data/repository/EpsRepositoryImpl.kt`
   - See: `EPS_INTEGRATION_GUIDE.md` for details

---

## 📱 What Works Now

✅ Open files via file picker  
✅ Preview with zoom/pan controls  
✅ Export dialog (UI ready)  
✅ Premium tier selection  
✅ Settings & preferences  
✅ Help documentation  
✅ All navigation flows  
✅ Billing & ad infrastructure  

⏳ **Not Yet:** EPS rendering (placeholder gray canvas - integrate real renderer)

---

## 🔧 Key Files to Customize

| File | Purpose |
|------|---------|
| `app/build.gradle.kts` | Dependencies, package name, version |
| `AndroidManifest.xml` | App name, permissions, deeplinks, ad unit |
| `strings.xml` | App name, labels, messages |
| `colors.xml` | Brand colors |
| `themes.xml` | Theme customization |
| `EpsRepositoryImpl.kt` | Rendering logic (INTEGRATE RENDERER HERE) |
| `BillingRepositoryImpl.kt` | Billing product IDs |

---

## 🎨 Branding Checklist

- [ ] Update app name in `strings.xml`
- [ ] Update colors in `colors.xml`
- [ ] Replace app icon in `drawable/ic_launcher_foreground.xml`
- [ ] Update mipmap densities (hdpi, xhdpi, xxhdpi, xxxhdpi)
- [ ] Update package name in `build.gradle.kts`
- [ ] Update app label in `AndroidManifest.xml`

---

## 🧪 Test Flows

### Home Screen
- Tap "Open EPS File" → File picker opens ✅
- Tap "Settings" → Settings screen opens ✅
- Tap "Go Premium" → Premium screen opens ✅
- Tap "Help" → Help webview loads ✅

### Viewer (after opening file)
- Preview loads (placeholder gray canvas) ⏳
- Zoom +/- buttons work ✅
- Fit/100% buttons work ✅
- Export menu shows options ✅

### Premium
- Monthly/Annual/Lifetime buttons ✅
- Clicking triggers billing flow ✅

---

## 🐛 Troubleshooting

### Build Fails
```
Error: "Cannot find com.example.epsviewer"
→ Clean project: Build → Clean Project
→ Sync Gradle: File → Sync Now
```

### Preview Doesn't Show
```
Current implementation is placeholder
→ Integrate real EPS renderer (see EPS_INTEGRATION_GUIDE.md)
```

### Ads Don't Show
```
Check AdMob ID in AndroidManifest.xml
Use test Ad Unit IDs in development
→ admob.google.com for real IDs
```

### File Picker Doesn't Open
```
Check Android version (min SDK 24)
Check READ_EXTERNAL_STORAGE permission for API <31
On API 30+, app has scoped storage access via SAF
```

---

## 📊 Project Structure at a Glance

```
testeps/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/epsviewer/
│   │   │   ├── app/EpsViewerApp.kt
│   │   │   ├── di/RepositoryModule.kt
│   │   │   ├── domain/
│   │   │   │   ├── model/ (Result, EpsMetadata, etc.)
│   │   │   │   └── repository/ (abstract interfaces)
│   │   │   ├── data/repository/ (implementations)
│   │   │   └── ui/
│   │   │       ├── splash/SplashActivity.kt
│   │   │       ├── home/HomeActivity.kt
│   │   │       ├── viewer/ViewerActivity.kt
│   │   │       ├── premium/PremiumActivity.kt
│   │   │       ├── settings/SettingsActivity.kt
│   │   │       └── help/HelpActivity.kt
│   │   ├── res/
│   │   │   ├── layout/ (6 activity layouts)
│   │   │   ├── drawable/ (vector icons)
│   │   │   ├── values/ (strings, colors, styles, arrays)
│   │   │   ├── xml/ (preferences, file paths)
│   │   │   └── mipmap-* (app icons)
│   │   └── assets/ (help.html, privacy_policy.html)
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── AndroidManifest.xml
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
├── CHANGELOG.md
├── LICENSE
├── EPS_INTEGRATION_GUIDE.md
└── PROJECT_SUMMARY.md
```

---

## 🎯 Next Steps

### Immediate (Before Testing)
1. Sync Gradle
2. Build debug APK
3. Run on device/emulator

### Short Term (Before Release)
1. Integrate EPS renderer
2. Configure Play Billing IDs
3. Configure AdMob ID
4. Update branding (name, colors, icons)
5. Test all flows

### Before Submission
1. Build release APK with minification
2. Sign with release keystore
3. Test on multiple devices
4. Get Play Store developer account
5. Create store listing
6. Upload to Play Console internal testing
7. Get feedback & fix issues
8. Release to production

---

## 💡 Tips

- **Use ProGuard in Debug** to catch issues early:
  ```gradle
  debug {
      isMinifyEnabled = true
  }
  ```

- **Test Billing Locally:**
  ```kotlin
  // In BillingRepositoryImpl, replace launchPurchaseFlow with:
  suspend fun launchPurchaseFlow(productId: String): Boolean {
      setTier(when {
          productId.contains("monthly") -> MONTHLY_SUBSCRIBER
          productId.contains("annual") -> ANNUAL_SUBSCRIBER
          productId.contains("lifetime") -> LIFETIME_SUBSCRIBER
          else -> FREE
      })
      return true // for testing
  }
  ```

- **Test Ads Locally:**
  ```kotlin
  // In AdMob settings, use these test Ad Unit IDs
  // Banner: ca-app-pub-3940256099942544/6300978111
  // Rewarded: ca-app-pub-3940256099942544/5224354917
  ```

---

## 📞 Need Help?

See detailed documentation:
- **Setup:** README.md
- **Features:** CHANGELOG.md
- **Integration:** EPS_INTEGRATION_GUIDE.md
- **Full Details:** PROJECT_SUMMARY.md

---

**Version:** 1.0.0  
**Status:** ✅ Ready to Customize & Deploy

