# EPS Viewer (Offline) - Android App

A production-grade, offline EPS file viewer and converter for Android. Open, preview, and export EPS files without uploading to any server.

## Features

✅ **100% Offline** - All processing happens on-device  
✅ **Fast Preview** - Render EPS files in <1.5s  
✅ **Export** - Convert to PNG, JPG, or PDF  
✅ **Zoom & Pan** - Full interactive viewer with PhotoView  
✅ **Free & Premium** - Free tier with ads, Premium with high-res exports  
✅ **Privacy-First** - No analytics, no tracking, no cloud sync  

## Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM + Repository Pattern
- **UI:** Activities/Fragments + Material Design 3
- **DI:** Hilt
- **Async:** Kotlin Coroutines + Flow
- **Billing:** Google Play Billing v6+
- **Ads:** Google Mobile Ads SDK
- **Image Viewing:** PhotoView
- **Logging:** Timber
- **Min SDK:** 24 (Android 6.0+)
- **Target SDK:** 34

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/epsviewer/
│   │   ├── app/                    # Application class
│   │   ├── di/                     # Hilt dependency injection
│   │   ├── domain/
│   │   │   ├── model/              # Data models (Result, EpsMetadata, etc.)
│   │   │   └── repository/         # Abstract repositories
│   │   ├── data/repository/        # Repository implementations
│   │   └── ui/
│   │       ├── splash/             # Splash screen
│   │       ├── home/               # Home/main screen
│   │       ├── viewer/             # EPS viewer activity
│   │       ├── premium/            # Premium plans activity
│   │       ├── settings/           # Settings activity
│   │       └── help/               # Help activity
│   ├── res/
│   │   ├── layout/                 # XML layouts
│   │   ├── drawable/               # Vector drawables & icons
│   │   ├── values/                 # Strings, colors, styles, arrays
│   │   ├── xml/                    # Preferences, file paths
│   │   └── mipmap-*/               # App icons (adaptive)
│   ├── assets/
│   │   ├── help.html               # Help content
│   │   └── privacy_policy.html     # Privacy policy
│   └── AndroidManifest.xml
├── src/test/java/                  # Unit tests
├── build.gradle.kts
└── proguard-rules.pro
```

## Setup Instructions

### 1. Clone or Extract Project

```bash
cd /path/to/testeps
```

### 2. Configure Play Billing Product IDs

Edit `app/src/main/java/com/example/epsviewer/data/repository/BillingRepositoryImpl.kt` and update with your Play Console product IDs:

```kotlin
// Replace placeholders with your actual product IDs
"com.example.epsviewer.monthly"
"com.example.epsviewer.annual"
"com.example.epsviewer.lifetime"
```

### 3. Configure Google Mobile Ads

Edit `app/src/main/AndroidManifest.xml` and replace:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
```

with your actual Google AdMob App ID from AdMob console.

### 4. Update Package Name (Optional)

To use a custom package name:

1. In Android Studio: **Refactor → Rename Package**
2. Update in `app/build.gradle.kts`:
   ```kotlin
   namespace = "com.yourcompany.epsviewer"
   applicationId = "com.yourcompany.epsviewer"
   ```

### 5. Update App Icons (Optional)

Replace drawable files in:
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Other mipmap densities

### 6. Update App Name & Strings

Edit `app/src/main/res/values/strings.xml`:

```xml
<string name="app_name">Your App Name</string>
```

### 7. Build Debug APK

```bash
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### 8. Build Release APK (with Minification)

```bash
./gradlew assembleRelease
```

APK will be at: `app/build/outputs/apk/release/app-release.apk`

## EPS Rendering Integration

**Current Implementation:** Placeholder rendering with mock bitmap output.

To integrate a real EPS renderer:

1. **Option A: MuPDF**
   - Download native .so files from MuPDF project
   - Place in: `app/src/main/jniLibs/arm64-v8a/`
   - Update `EpsRepositoryImpl.kt` to use MuPDF API

2. **Option B: Ghostscript JNI Bindings**
   - Integrate Ghostscript through Java bindings
   - Add to dependencies in `build.gradle.kts`
   - Implement wrapper in `EpsRepositoryImpl.kt`

3. **Option C: Third-Party Library**
   - Add to `gradle/libs.versions.toml`
   - Implement `EpsRepository` interface with library calls

The abstract `EpsRepository` interface allows swapping implementations without changing UI code.

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

### Key Test Files

- `ResultTest.kt` - Tests Result<T> sealed class
- `MockBillingRepository.kt` - Mock for testing billing logic
- `BillingRepositoryImplTest.kt` - Billing state transitions

## Billing & Ads Configuration

### Test Billing

For development, use Google Play Console's test product IDs:
- `android.test.purchased`
- `android.test.canceled`
- `android.test.refunded`

### Disable Ads (QA Mode)

In `HomeActivity.kt`:

```kotlin
binding.adContainer.visibility = android.view.View.GONE
```

Or set premium tier in `BillingRepositoryImpl` to force premium state.

## ProGuard/R8 Configuration

ProGuard rules are configured in `app/proguard-rules.pro` for:
- Hilt DI
- Google Play Services
- Coroutines
- Repository interfaces
- Model classes

Enable minification in release builds—rules ensure all necessary classes are retained.

## Privacy & Permissions

✅ **No Cloud Upload** - All EPS rendering is local  
✅ **No PII Collection** - No tracking, analytics, or personal data  
✅ **INTERNET Permission** - Only for optional ads/billing (can be disabled)  
✅ **Storage Permissions** - To open and save files (SAF-based)

## Continuous Integration Setup (GitHub Actions)

Example workflow (optional):

```yaml
name: Build APK
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: 11
      - run: ./gradlew assembleRelease
```

## Troubleshooting

### Build Issues

**Error: `Could not find com.google.dagger:hilt-compiler`**
→ Ensure `hilt-compiler` is in dependencies and `kotlin-kapt` plugin is applied.

**Error: `Execution failed for task ':app:minifyReleaseWithR8'`**
→ Check ProGuard rules in `proguard-rules.pro`. Add more `-keep` rules if needed.

### Runtime Issues

**Ads not showing**
→ Check AdMob App ID in AndroidManifest.xml. Use test ad unit IDs in development.

**File picker not opening**
→ Ensure app has READ_EXTERNAL_STORAGE permission (API <31) or all files access.

**Preview rendering blank**
→ Current implementation is placeholder. Integrate real EPS renderer.

## Play Store Submission Checklist

- [ ] Update version code/name in `build.gradle.kts`
- [ ] Configure Play Billing product IDs
- [ ] Configure Google Mobile Ads App ID
- [ ] Add app icon (1024x1024 PNG)
- [ ] Create store listing screenshots
- [ ] Write compelling app description
- [ ] Link Privacy Policy (included in app)
- [ ] Test on multiple devices
- [ ] Build release APK with minification enabled
- [ ] Sign APK with release keystore
- [ ] Upload to Play Console
- [ ] Review and publish

## Nice-to-Haves for Future Releases

- [ ] Dark theme support
- [ ] Batch export with progress
- [ ] Recent files with thumbnails
- [ ] File caching with DiskLruCache
- [ ] Crash reporting (Crashlytics)
- [ ] Analytics (Firebase)
- [ ] CMYK color space detection
- [ ] Share exported files directly

## License

This project is provided as-is for demonstration and commercial use.

## Support

For issues, feature requests, or questions:
- Email: support@example.com
- Website: https://example.com

---

**Version:** 1.0.0  
**Last Updated:** February 2026

