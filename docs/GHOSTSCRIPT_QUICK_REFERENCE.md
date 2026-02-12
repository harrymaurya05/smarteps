# Ghostscript Quick Reference

## What is Ghostscript?

Ghostscript is an interpreter for PostScript and PDF. It's used to:
- Render EPS/PostScript files to images (PNG, JPEG)
- Convert EPS to PDF
- Extract text and metadata
- Process PostScript commands

## Current Status

✅ **Kotlin wrapper implemented** (`GhostscriptWrapper.kt`)  
✅ **JNI bridge prepared** (`ghostscript_jni.cpp`)  
✅ **Build system configured** (`CMakeLists.txt`)  
✅ **Integrated with EpsRenderer** (automatic fallback)  

⚠️ **Missing native library** (`libgs.so`)  
⚠️ **Missing headers** (`iapi.h`, `ierrors.h`)  

## Quick Commands

### Check if Ghostscript is available

```kotlin
val gs = GhostscriptWrapper(context)
if (gs.isAvailable()) {
    val version = gs.getVersion()
    Log.d("GS", "Version: $version")
}
```

### Render EPS to PNG

```kotlin
val success = gs.renderToPng(
    inputPath = "/storage/emulated/0/input.eps",
    outputPath = "/storage/emulated/0/output.png",
    dpi = 150
)
```

### Convert EPS to PDF

```kotlin
val success = gs.convertToPdf(
    inputPath = "/storage/emulated/0/input.eps",
    outputPath = "/storage/emulated/0/output.pdf"
)
```

### Render to JPEG

```kotlin
val success = gs.renderToJpeg(
    inputPath = "/storage/emulated/0/input.eps",
    outputPath = "/storage/emulated/0/output.jpg",
    dpi = 300,
    quality = 90
)
```

## Common DPI Values

| DPI | Use Case | File Size |
|-----|----------|-----------|
| 72  | Screen preview | Small |
| 150 | Standard quality | Medium |
| 300 | Print quality | Large |
| 600 | High-res print | Very large |

## Ghostscript Devices

| Device | Output Format | Use |
|--------|---------------|-----|
| `png16m` | PNG (24-bit RGB) | Color images |
| `pngalpha` | PNG with transparency | Transparent backgrounds |
| `jpeg` | JPEG | Photos, compressed |
| `pdfwrite` | PDF | Vector preservation |
| `txtwrite` | Text | Text extraction |
| `bbox` | Bounding box | Dimensions only |

## File Locations

```
app/
├── src/main/
│   ├── cpp/
│   │   ├── CMakeLists.txt           # Build config
│   │   └── ghostscript_jni.cpp      # Native bridge
│   ├── java/.../util/
│   │   ├── GhostscriptWrapper.kt    # Kotlin API
│   │   └── EpsRenderer.kt           # Uses GS
│   └── jniLibs/                     # Add native libs here
│       ├── arm64-v8a/
│       │   └── libgs.so            # ARM64 (modern phones)
│       ├── armeabi-v7a/
│       │   └── libgs.so            # ARM32 (older phones)
│       ├── x86/
│       │   └── libgs.so            # Emulator (32-bit)
│       └── x86_64/
│           └── libgs.so            # Emulator (64-bit)
```

## Adding Ghostscript Library

### Step 1: Obtain libgs.so

**Option A: Build from source** (see GHOSTSCRIPT_INTEGRATION.md)  
**Option B: Download pre-built** (community builds)  
**Option C: Use commercial SDK** (Artifex/MuPDF)

### Step 2: Copy to project

```bash
mkdir -p app/src/main/jniLibs/arm64-v8a
cp /path/to/libgs.so app/src/main/jniLibs/arm64-v8a/
```

### Step 3: Add headers

```bash
mkdir -p app/src/main/cpp/include/ghostscript
cp iapi.h app/src/main/cpp/include/ghostscript/
cp ierrors.h app/src/main/cpp/include/ghostscript/
```

### Step 4: Update CMakeLists.txt

Uncomment these lines:

```cmake
find_library(ghostscript-lib gs PATHS ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
target_link_libraries(gs ${ghostscript-lib})
target_include_directories(gs PRIVATE ${CMAKE_SOURCE_DIR}/include)
```

### Step 5: Update native code

In `ghostscript_jni.cpp`, uncomment:
- `#include "ghostscript/iapi.h"`
- `#include "ghostscript/ierrors.h"`
- All Ghostscript API calls in functions

### Step 6: Build

```bash
./gradlew clean assembleDebug
```

## Testing

### Test without Ghostscript

App works now with built-in renderer:

```bash
./gradlew installDebug
adb shell am start -n com.example.epsviewer/.MainActivity
```

### Test with Ghostscript

After adding libgs.so:

```bash
./gradlew clean assembleDebug installDebug
adb logcat | grep -E "(GhostscriptJNI|GhostscriptWrapper)"
```

## Troubleshooting

### Library not found

```
UnsatisfiedLinkError: dlopen failed: library "libgs.so" not found
```

**Fix**: Copy libgs.so to `app/src/main/jniLibs/{ABI}/`

### Method not found

```
UnsatisfiedLinkError: No implementation found for boolean ...
```

**Fix**: Rebuild with `./gradlew clean assembleDebug`

### Crashes on render

```
Fatal signal 11 (SIGSEGV)
```

**Fix**: 
- Verify EPS file is valid
- Check paths are absolute
- Enable logging in native code

## Logging

### Kotlin level

```kotlin
Timber.d("Rendering with Ghostscript")
```

### Native level

```cpp
LOGD("Ghostscript executing with %d args", argc);
```

### View logs

```bash
adb logcat | grep -E "(GhostscriptJNI|EpsRenderer|GhostscriptWrapper)"
```

## License Warning

⚠️ **Ghostscript is AGPL v3**

- Free for open source projects only
- Commercial use requires paid license
- Your app must be open source if using AGPL
- Alternative: Purchase commercial license from Artifex

## Performance Tips

1. **Cache rendered images** - Don't re-render same file
2. **Use appropriate DPI** - Higher = slower + larger files
3. **Render in background** - Use coroutines/WorkManager
4. **Cleanup temp files** - Delete after use
5. **Limit memory** - Recycle bitmaps when done

## Next Steps

1. ✅ Code is ready - just needs libgs.so
2. 📥 Download or build Ghostscript for Android
3. 📂 Copy libgs.so to jniLibs/
4. 📝 Uncomment native code
5. 🔨 Rebuild project
6. ✨ Enjoy high-quality EPS rendering!

## Resources

- Full guide: [GHOSTSCRIPT_INTEGRATION.md](./GHOSTSCRIPT_INTEGRATION.md)
- Ghostscript official: https://www.ghostscript.com/
- Source code: https://github.com/ArtifexSoftware/ghostpdl
- Commercial license: https://www.artifex.com/

## Support

Questions? Check:
1. [GHOSTSCRIPT_INTEGRATION.md](./GHOSTSCRIPT_INTEGRATION.md) - Complete guide
2. Logs: `adb logcat | grep Ghostscript`
3. GitHub issues
4. Stack Overflow: [ghostscript-android]

---

**TL;DR**: Code is ready. Add `libgs.so` to `app/src/main/jniLibs/arm64-v8a/` and rebuild. Until then, built-in renderer works for basic EPS files.

