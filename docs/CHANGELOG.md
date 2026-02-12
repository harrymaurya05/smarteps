# Changelog

All notable changes to EPS Viewer (Offline) will be documented in this file.

## [1.0.0] - 2026-02-12

### Added

#### Core Features
- ✨ Open EPS files via Android Storage Access Framework (SAF)
- ✨ Fast preview rendering with zoom, pan, fit-to-screen, and actual-size controls
- ✨ Export to PNG and JPG formats at configurable DPI (150-600)
- ✨ File information dialog (name, size, dimensions)
- ✨ Recent files list (placeholder for future implementation)

#### Premium Features
- 💎 High-resolution exports (300-600 DPI) for PNG, JPG, and PDF
- 💎 Batch export functionality (API contract, UI placeholder)
- 💎 Ad-free experience
- 💎 Monthly, Annual, and Lifetime subscription options

#### Free Tier
- 📊 Bottom banner ads (non-intrusive)
- 📊 Optional rewarded ad for one-time high-res export
- 📊 Standard resolution exports (150 DPI)

#### User Experience
- 🎨 Material Design 3 UI with adaptive icons
- 🎨 Splash screen with brand animation
- 🎨 Settings screen with preferences
  - Theme selection (Light, Dark, System Default)
  - Default export format preference
  - Default DPI preference
  - Clear cache option
- 🎨 Help screen with complete usage instructions and offline assurance
- 🎨 About section with version and license information
- 🎨 Privacy Policy displayed in-app (webview)

#### Technical Features
- 🏗️ MVVM architecture with Repository pattern
- 🏗️ Hilt dependency injection
- 🏗️ Kotlin Coroutines + Flow for async operations
- 🏗️ Google Play Billing v6+ integration
- 🏗️ Google Mobile Ads SDK integration
- 🏗️ Timber logging
- 🏗️ PhotoView for interactive image viewing
- 🏗️ Data binding and view binding
- 🏗️ Abstract EPS rendering repository for easy implementation swapping

#### Privacy & Security
- 🔒 100% offline operation—no file uploads to any server
- 🔒 No personal data collection or tracking
- 🔒 No analytics or crash reporting
- 🔒 Clear privacy policy explaining INTERNET permission (ads/billing only)
- 🔒 SAF-based file access—no legacy storage permissions required
- 🔒 ProGuard/R8 minification enabled for release builds

#### Code Quality
- 📝 Comprehensive Kotlin documentation
- 📝 Unit tests for Result<T> sealed class
- 📝 Mock billing repository for testing
- 📝 Billing state transition tests
- 📝 ProGuard rules for all major dependencies
- 📝 Gradle configuration with version catalog

#### Documentation
- 📖 Complete README.md with setup instructions
- 📖 License information
- 📖 Integration guide for EPS renderer (MuPDF, Ghostscript, etc.)
- 📖 Play Store submission checklist
- 📖 Troubleshooting section

### Known Limitations

- EPS rendering is placeholder (gray canvas). Integrate MuPDF or Ghostscript for production.
- Recent files list not persisted (database integration can be added)
- Batch export UI is complete but logic is placeholder
- No dark theme implementation yet (infrastructure in place)
- No crash reporting or analytics (can be wired in future)

### Future Releases

- [ ] Real EPS renderer integration (MuPDF/Ghostscript)
- [ ] Thumbnail caching with DiskLruCache
- [ ] Recent files persistence with Room database
- [ ] Dark theme implementation
- [ ] Crash reporting (Firebase Crashlytics)
- [ ] Analytics (Firebase Analytics)
- [ ] Batch export progress indicator
- [ ] File sharing directly from app
- [ ] CMYK → sRGB color space conversion
- [ ] Support for multi-page PS/PDF files

---

## Versioning

Semantic Versioning: MAJOR.MINOR.PATCH

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes

---

**Current Version:** 1.0.0  
**Minimum Android:** SDK 24 (Android 6.0+)  
**Target Android:** SDK 34 (Android 14+)

