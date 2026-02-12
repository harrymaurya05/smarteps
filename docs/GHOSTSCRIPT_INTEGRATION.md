# Ghostscript Integration Guide

## Overview

This EPS Viewer app integrates **Ghostscript** for high-quality rendering and conversion of EPS (Encapsulated PostScript) files. Ghostscript is the industry-standard interpreter for PostScript and PDF.

## Table of Contents

1. [Architecture](#architecture)
2. [Installation](#installation)
3. [Building Ghostscript for Android](#building-ghostscript-for-android)
4. [Using Pre-built Libraries](#using-pre-built-libraries)
5. [Integration](#integration)
6. [Usage Examples](#usage-examples)
7. [Testing](#testing)
8. [Troubleshooting](#troubleshooting)
9. [License](#license)

---

## Architecture

The integration consists of three layers:

```
┌─────────────────────────────────────┐
│   Kotlin/Java Layer                 │
│   (GhostscriptWrapper.kt)           │
│   • High-level API                  │
│   • Error handling                  │
│   • File I/O management             │
└────────────┬────────────────────────┘
             │ JNI
┌────────────▼────────────────────────┐
│   Native C++ Layer                  │
│   (ghostscript_jni.cpp)             │
│   • JNI bridge                      │
│   • Argument marshalling            │
│   • Logging                         │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   Ghostscript C API                 │
│   (libgs.so)                        │
│   • PostScript interpreter          │
│   • Device drivers (PNG, PDF, etc.) │
│   • Rendering engine                │
└─────────────────────────────────────┘
```

### Files Structure

```
app/
├── src/main/
│   ├── cpp/
│   │   ├── CMakeLists.txt              # Build configuration
│   │   └── ghostscript_jni.cpp         # JNI bridge implementation
│   ├── java/com/example/epsviewer/util/
│   │   ├── GhostscriptWrapper.kt       # Kotlin wrapper
│   │   └── EpsRenderer.kt              # Uses Ghostscript for rendering
│   └── jniLibs/                        # Native libraries (to be added)
│       ├── arm64-v8a/
│       │   └── libgs.so
│       ├── armeabi-v7a/
│       │   └── libgs.so
│       ├── x86/
│       │   └── libgs.so
│       └── x86_64/
│           └── libgs.so
```

---

## Installation

### Prerequisites

- Android NDK r21 or later
- CMake 3.22.1 or later
- Android Studio Arctic Fox or later
- Ghostscript 10.0+ source or pre-built binaries

---

## Building Ghostscript for Android

Ghostscript doesn't provide official Android builds, so you need to compile it yourself or use community builds.

### Option 1: Build from Source (Advanced)

#### Step 1: Download Ghostscript Source

```bash
cd ~/Downloads
wget https://github.com/ArtifexSoftware/ghostpdl-downloads/releases/download/gs10021/ghostscript-10.02.1.tar.gz
tar -xzf ghostscript-10.02.1.tar.gz
cd ghostscript-10.02.1
```

#### Step 2: Set Up Android NDK

```bash
export ANDROID_NDK_HOME=/path/to/android-ndk-r25c
export TOOLCHAIN=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/darwin-x86_64
```

#### Step 3: Configure for Android (ARM64)

```bash
export CC=$TOOLCHAIN/bin/aarch64-linux-android30-clang
export CXX=$TOOLCHAIN/bin/aarch64-linux-android30-clang++
export AR=$TOOLCHAIN/bin/llvm-ar
export RANLIB=$TOOLCHAIN/bin/llvm-ranlib

./configure \
    --host=aarch64-linux-android \
    --prefix=/usr/local \
    --disable-gtk \
    --disable-cups \
    --without-x \
    --with-drivers=PNG,PDFWRITE \
    --enable-shared=no \
    --enable-static=yes
```

#### Step 4: Build

```bash
make -j8
```

#### Step 5: Create Shared Library

```bash
$CC -shared -o libgs.so \
    -Wl,--whole-archive \
    sobin/*.o \
    -Wl,--no-whole-archive \
    -lm -ldl -lz
```

#### Step 6: Copy to Project

```bash
mkdir -p ~/Downloads/testeps/app/src/main/jniLibs/arm64-v8a
cp libgs.so ~/Downloads/testeps/app/src/main/jniLibs/arm64-v8a/
```

#### Step 7: Repeat for Other Architectures

Repeat steps 3-6 for:
- `armeabi-v7a`: `armv7a-linux-androideabi30-clang`
- `x86`: `i686-linux-android30-clang`
- `x86_64`: `x86_64-linux-android30-clang`

---

### Option 2: Use Pre-built Libraries (Easier)

Some community projects provide pre-built Ghostscript for Android:

#### MuPDF Commercial SDK
- Includes Ghostscript
- https://www.artifex.com/products/mupdf/

#### Community Builds
- Search GitHub for "ghostscript android"
- Example: https://github.com/ArtifexSoftware/ghostpdl-android

---

## Using Pre-built Libraries

### Step 1: Download or Build libgs.so

Obtain `libgs.so` for your target architectures.

### Step 2: Add to Project

```bash
mkdir -p app/src/main/jniLibs/arm64-v8a
cp /path/to/libgs.so app/src/main/jniLibs/arm64-v8a/

# Repeat for other architectures
```

### Step 3: Update CMakeLists.txt

Edit `app/src/main/cpp/CMakeLists.txt`:

```cmake
# Uncomment these lines after adding libgs.so:
find_library(
    ghostscript-lib
    gs
    PATHS ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}
    NO_DEFAULT_PATH
)

target_link_libraries(
    gs
    ${log-lib}
    ${ghostscript-lib}
)
```

### Step 4: Add Ghostscript Headers

Download Ghostscript headers from the source distribution:

```bash
mkdir -p app/src/main/cpp/include/ghostscript
cp ghostscript-10.02.1/psi/iapi.h app/src/main/cpp/include/ghostscript/
cp ghostscript-10.02.1/psi/ierrors.h app/src/main/cpp/include/ghostscript/
```

Update CMakeLists.txt:

```cmake
target_include_directories(
    gs
    PRIVATE
    ${CMAKE_SOURCE_DIR}/include
)
```

### Step 5: Uncomment Native Code

In `ghostscript_jni.cpp`, uncomment:

```cpp
#include "ghostscript/iapi.h"
#include "ghostscript/ierrors.h"
```

And uncomment the actual Ghostscript API calls in each function.

### Step 6: Build Project

```bash
cd ~/Downloads/testeps
./gradlew assembleDebug
```

---

## Integration

### Current Status

The app is **ready for Ghostscript integration** with the following components already in place:

✅ `GhostscriptWrapper.kt` - Kotlin JNI wrapper  
✅ `ghostscript_jni.cpp` - Native bridge (placeholder)  
✅ `CMakeLists.txt` - Build configuration  
✅ `EpsRenderer.kt` - Uses Ghostscript when available  
✅ Gradle configuration - NDK support enabled  

### What's Missing

⚠️ **Ghostscript native library** (`libgs.so`)  
⚠️ **Ghostscript headers** (`iapi.h`, `ierrors.h`)  

### Fallback Behavior

Without Ghostscript, the app uses a **built-in PostScript renderer** that handles basic vector commands. This provides limited rendering capabilities.

---

## Usage Examples

### Basic Rendering

```kotlin
val ghostscript = GhostscriptWrapper(context)

// Check availability
if (ghostscript.isAvailable()) {
    val version = ghostscript.getVersion()
    Log.d("GS", "Ghostscript version: $version")
    
    // Render EPS to PNG
    val success = ghostscript.renderToPng(
        inputPath = "/path/to/input.eps",
        outputPath = "/path/to/output.png",
        dpi = 150
    )
    
    if (success) {
        val bitmap = BitmapFactory.decodeFile("/path/to/output.png")
        imageView.setImageBitmap(bitmap)
    }
} else {
    Log.w("GS", "Ghostscript not available, using fallback renderer")
}
```

### Convert to PDF

```kotlin
val epsFile = File("/path/to/document.eps")
val pdfFile = File("/path/to/output.pdf")

val renderer = EpsRenderer(context)
val success = renderer.convertToPdf(epsFile, pdfFile)

if (success) {
    Log.i("Export", "PDF created: ${pdfFile.absolutePath}")
}
```

### Render to JPEG

```kotlin
ghostscript.renderToJpeg(
    inputPath = "/path/to/image.eps",
    outputPath = "/path/to/output.jpg",
    dpi = 300,
    quality = 95
)
```

### Advanced: Custom Commands

```kotlin
val args = arrayOf(
    "-dNOPAUSE",
    "-dBATCH",
    "-dSAFER",
    "-sDEVICE=png16m",
    "-r300",
    "-dTextAlphaBits=4",
    "-dGraphicsAlphaBits=4",
    "-o/path/to/output.png",
    "/path/to/input.eps"
)

val success = ghostscript.executeCustom(args)
```

---

## Testing

### Unit Tests

Test Ghostscript integration:

```kotlin
@Test
fun testGhostscriptAvailability() {
    val gs = GhostscriptWrapper(context)
    // Should return true if libgs.so is present
    assertTrue(gs.isAvailable())
}

@Test
fun testRenderToPng() {
    val gs = GhostscriptWrapper(context)
    assumeTrue(gs.isAvailable())
    
    val input = copyAssetToCache("test.eps")
    val output = File(context.cacheDir, "test_output.png")
    
    val success = gs.renderToPng(input.absolutePath, output.absolutePath, 150)
    
    assertTrue(success)
    assertTrue(output.exists())
    assertTrue(output.length() > 0)
}
```

### Manual Testing

1. **Test with Sample EPS Files**
   - Place sample EPS files in `app/src/main/assets/samples/`
   - Load and render in the app

2. **Test Different DPI Values**
   - Try 72, 150, 300, 600 DPI
   - Verify output quality vs. file size

3. **Test PDF Conversion**
   - Convert EPS to PDF
   - Open in PDF viewer
   - Verify dimensions and quality

---

## Troubleshooting

### Problem: UnsatisfiedLinkError

```
java.lang.UnsatisfiedLinkError: dlopen failed: library "libgs.so" not found
```

**Solution:**
- Ensure `libgs.so` is in `app/src/main/jniLibs/{ABI}/`
- Check ABI matches device (arm64-v8a for most modern devices)
- Verify library is not stripped during build

### Problem: Native method not found

```
java.lang.UnsatisfiedLinkError: No implementation found for boolean com.example...
```

**Solution:**
- Rebuild native libraries: `./gradlew clean assembleDebug`
- Verify JNI method signatures match Kotlin declarations
- Check CMakeLists.txt includes all source files

### Problem: Ghostscript crashes

```
A/libc: Fatal signal 11 (SIGSEGV), code 1
```

**Solution:**
- Check input file is valid EPS
- Ensure sufficient memory
- Verify Ghostscript version compatibility
- Add error checking in native code

### Problem: Output file is empty

**Solution:**
- Check input file path is correct (absolute path required)
- Verify output directory exists and is writable
- Check Ghostscript return code
- Enable debug logging in native code

### Problem: Build fails with CMake errors

```
CMake Error: Could not create named generator Android Gradle
```

**Solution:**
- Update Android Studio to latest version
- Install NDK and CMake via SDK Manager
- Verify CMake version in CMakeLists.txt matches installed version

---

## License

### Ghostscript License

Ghostscript is dual-licensed:

1. **GNU Affero General Public License (AGPL) v3**
   - **Free** for open source projects
   - Requires your app to be open source under AGPL
   - Source code must be provided to users

2. **Commercial License**
   - Purchase from Artifex Software
   - Allows proprietary use
   - No source code disclosure required
   - See: https://www.artifex.com/

### This Project

This EPS Viewer app is provided as a demonstration. If you distribute it with Ghostscript:

- **Open Source Distribution**: Use AGPL v3 license
- **Commercial Distribution**: Purchase commercial Ghostscript license

### Important Notes

⚠️ **GPL/AGPL is viral**: All code that uses Ghostscript must be GPL/AGPL  
⚠️ **Network distribution triggers AGPL**: Serving over network requires source disclosure  
⚠️ **Commercial use**: Consider purchasing commercial license  

---

## Resources

### Official Documentation

- **Ghostscript Home**: https://www.ghostscript.com/
- **Ghostscript Docs**: https://ghostscript.com/docs/9.56.1/Use.htm
- **Device Drivers**: https://ghostscript.com/docs/9.56.1/Devices.htm
- **API Reference**: https://ghostscript.com/docs/9.56.1/API.htm

### Community Resources

- **Artifex GitHub**: https://github.com/ArtifexSoftware/ghostpdl
- **Stack Overflow**: [ghostscript-android] tag
- **Mailing List**: gs-devel@ghostscript.com

### Alternative Solutions

If Ghostscript licensing is an issue:

1. **MuPDF**: AGPL or commercial (includes Ghostscript)
2. **Poppler**: GPL (PDF only, no EPS)
3. **Apache PDFBox**: Apache 2.0 (PDF only)
4. **Commercial APIs**: PSPDFKit, Foxit

---

## Next Steps

### Quick Start (Without Ghostscript)

The app works now with the built-in renderer:

```bash
cd ~/Downloads/testeps
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Full Integration (With Ghostscript)

1. Build or download `libgs.so` for Android
2. Copy to `app/src/main/jniLibs/{ABI}/`
3. Add Ghostscript headers to `app/src/main/cpp/include/`
4. Uncomment native code in `ghostscript_jni.cpp`
5. Update `CMakeLists.txt` to link Ghostscript
6. Rebuild and test

### Testing Without Ghostscript

The built-in renderer handles:
- ✅ Basic vector commands (moveto, lineto, arc)
- ✅ Stroke and fill operations
- ✅ Color (RGB)
- ✅ Simple PostScript files

It does **not** handle:
- ❌ Complex PostScript (loops, procedures)
- ❌ Fonts and text rendering
- ❌ Image data (hex-encoded bitmaps)
- ❌ Advanced operators

---

## Support

For issues with this integration:
1. Check [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
2. Review logs: `adb logcat | grep GhostscriptJNI`
3. Open GitHub issue with details

For Ghostscript-specific questions:
- Visit https://www.ghostscript.com/support.html
- Contact Artifex support (commercial license holders)

---

**Last Updated**: February 2026  
**Ghostscript Version**: 10.02.1  
**Android NDK**: r25c  
**Min Android SDK**: 24 (Android 7.0)

