# Native Libraries Directory

This directory contains native shared libraries (.so files) for different Android architectures.

## Structure

```
jniLibs/
├── arm64-v8a/          # 64-bit ARM (most modern devices)
│   └── libgs.so        # Ghostscript library
├── armeabi-v7a/        # 32-bit ARM (older devices)
│   └── libgs.so
├── x86/                # 32-bit x86 (emulators)
│   └── libgs.so
└── x86_64/             # 64-bit x86 (emulators)
    └── libgs.so
```

## Required Library

**libgs.so** - Ghostscript PostScript/PDF interpreter

This library is **NOT included** due to:
- Large file size (~10+ MB per architecture)
- Licensing requirements (AGPL v3)
- Build complexity

## How to Add

### Option 1: Build from Source

See [GHOSTSCRIPT_INTEGRATION.md](../../../docs/GHOSTSCRIPT_INTEGRATION.md) for detailed build instructions.

### Option 2: Download Pre-built

1. Find community builds on GitHub:
   ```bash
   # Search for "ghostscript android" or "libgs.so android"
   ```

2. Download for each architecture

3. Copy to corresponding directory:
   ```bash
   cp libgs.so app/src/main/jniLibs/arm64-v8a/
   cp libgs.so app/src/main/jniLibs/armeabi-v7a/
   # etc.
   ```

### Option 3: Commercial SDK

Purchase from Artifex Software:
- https://www.artifex.com/
- Includes Ghostscript + support
- No AGPL restrictions

## Verification

After adding libgs.so:

```bash
# Check file exists
ls -lh app/src/main/jniLibs/arm64-v8a/libgs.so

# Check architecture
file app/src/main/jniLibs/arm64-v8a/libgs.so
# Should output: ELF 64-bit LSB shared object, ARM aarch64...

# Check symbols
nm -D app/src/main/jniLibs/arm64-v8a/libgs.so | grep gsapi
# Should show gsapi_* functions
```

## App Behavior

### With libgs.so (Recommended)
- ✅ High-quality EPS rendering
- ✅ Full PostScript support
- ✅ PDF conversion
- ✅ Image-based EPS files
- ✅ Complex graphics

### Without libgs.so (Current)
- ⚠️ Built-in basic renderer
- ⚠️ Limited PostScript support
- ⚠️ No PDF conversion
- ⚠️ Simple vector graphics only

The app automatically detects Ghostscript availability and falls back to the built-in renderer if not found.

## File Sizes (Approximate)

| Architecture | libgs.so Size |
|--------------|---------------|
| arm64-v8a    | 12-15 MB      |
| armeabi-v7a  | 10-12 MB      |
| x86          | 12-14 MB      |
| x86_64       | 14-16 MB      |

**Total**: ~50-60 MB for all architectures

Consider including only `arm64-v8a` and `armeabi-v7a` for production (covers 99%+ of real devices).

## License Notice

⚠️ **Ghostscript is licensed under AGPL v3**

If you distribute this app with libgs.so:
- You **must** open source your entire app
- You **must** provide source code to users
- Or purchase a commercial license from Artifex

See [LICENSE](../../../docs/LICENSE) for details.

## Build Integration

The native bridge is already implemented:
- ✅ JNI wrapper: `GhostscriptWrapper.kt`
- ✅ Native code: `ghostscript_jni.cpp`
- ✅ Build config: `CMakeLists.txt`
- ✅ Gradle config: `build.gradle.kts`

Just add libgs.so and rebuild:

```bash
./gradlew clean assembleDebug
```

## Troubleshooting

### UnsatisfiedLinkError

```
java.lang.UnsatisfiedLinkError: dlopen failed: library "libgs.so" not found
```

**Solutions**:
1. Verify libgs.so exists in correct directory
2. Check architecture matches device (use `adb shell getprop ro.product.cpu.abi`)
3. Ensure file permissions are correct (should be readable)
4. Clean and rebuild project

### Wrong Architecture

```
java.lang.UnsatisfiedLinkError: dlopen failed: ... wrong ELF class: ELFCLASS32
```

**Solution**: You're using 32-bit library on 64-bit device. Use correct architecture.

### Missing Symbols

```
java.lang.UnsatisfiedLinkError: ... undefined symbol: gsapi_new_instance
```

**Solution**: libgs.so is not properly built or stripped. Get complete build with all symbols.

## Resources

- Full integration guide: [GHOSTSCRIPT_INTEGRATION.md](../../../docs/GHOSTSCRIPT_INTEGRATION.md)
- Quick reference: [GHOSTSCRIPT_QUICK_REFERENCE.md](../../../docs/GHOSTSCRIPT_QUICK_REFERENCE.md)
- Ghostscript official: https://www.ghostscript.com/

## Next Steps

1. Obtain libgs.so for Android
2. Copy to this directory (architecture-specific folders)
3. Add Ghostscript headers to `cpp/include/`
4. Uncomment native code in `ghostscript_jni.cpp`
5. Rebuild project
6. Test with sample EPS files

---

**Status**: Ready for library integration  
**Required**: libgs.so (not included)  
**Fallback**: Built-in renderer (active)

