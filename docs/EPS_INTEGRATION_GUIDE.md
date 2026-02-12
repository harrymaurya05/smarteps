## Integration Guide: EPS Rendering

This app is designed with an abstract `EpsRepository` interface to allow easy swapping of different EPS rendering implementations. By default, a placeholder implementation is provided.

## Option 1: MuPDF Integration (Recommended)

### Step 1: Download MuPDF JNI Bindings

```bash
# Download from MuPDF official source
# Version: mupdf-1.23.0 or later
# Get Android NDK binaries
```

### Step 2: Add Native Libraries

```bash
mkdir -p app/src/main/jniLibs/arm64-v8a
cp libmupdf.so app/src/main/jniLibs/arm64-v8a/
```

### Step 3: Add MuPDF Dependency

Edit `gradle/libs.versions.toml`:
```toml
[versions]
mupdf = "1.23.0"

[libraries]
mupdf-core = { group = "com.artifex.mupdf", name = "mupdf-core", version.ref = "mupdf" }
```

Edit `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.mupdf.core)
}
```

### Step 4: Implement EPS Rendering

Replace `EpsRepositoryImpl.kt` with MuPDF calls:

```kotlin
override suspend fun renderPreview(
    inputUri: Uri,
    scale: Float,
    bgColor: Int
): Result<Bitmap> = withContext(Dispatchers.Default) {
    try {
        val document = Document.openDocument(inputUri.path)
        val page = document.loadPage(0)
        val bitmap = page.toPixmap(scale, bgColor)
        Result.Success(bitmap)
    } catch (e: Exception) {
        Result.Error(e, "MuPDF rendering failed")
    }
}
```

### Step 5: Update ProGuard Rules

Add to `proguard-rules.pro`:
```
-keep class com.artifex.mupdf.** { *; }
-keep native methods;
```

---

## Option 2: Ghostscript Integration

### Step 1: Get Ghostscript JNI Library

```bash
# Download Ghostscript for Android
# Build or download pre-compiled .so files
mkdir -p app/src/main/jniLibs/arm64-v8a
cp libgs.so app/src/main/jniLibs/arm64-v8a/
```

### Step 2: Create Ghostscript Wrapper

```kotlin
// In data/repository/GhostscriptRenderer.kt
class GhostscriptRenderer {
    external fun renderEps(inputPath: String, outputPath: String, dpi: Int): Boolean
    
    companion object {
        init {
            System.loadLibrary("gs")
        }
    }
}
```

### Step 3: Implement EPS Rendering

Update `EpsRepositoryImpl.kt`:

```kotlin
override suspend fun renderPreview(
    inputUri: Uri,
    scale: Float,
    bgColor: Int
): Result<Bitmap> = withContext(Dispatchers.IO) {
    try {
        val gsRenderer = GhostscriptRenderer()
        val tempFile = File(context.cacheDir, "eps_preview.png")
        
        val success = gsRenderer.renderEps(
            inputUri.path ?: return@withContext Result.Error(Exception("Invalid URI"), ""),
            tempFile.absolutePath,
            150
        )
        
        if (success && tempFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
            tempFile.delete()
            Result.Success(bitmap)
        } else {
            Result.Error(Exception("Ghostscript failed"), "Rendering failed")
        }
    } catch (e: Exception) {
        Result.Error(e, "Ghostscript error")
    }
}
```

---

## Option 3: Third-Party Android Library

### Example: EPS-Render Library (hypothetical)

Edit `gradle/libs.versions.toml`:
```toml
eps-render = { group = "com.example", name = "eps-render", version = "1.0" }
```

Edit `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.eps.render)
}
```

Implement in `EpsRepositoryImpl.kt`:
```kotlin
override suspend fun renderPreview(
    inputUri: Uri,
    scale: Float,
    bgColor: Int
): Result<Bitmap> = withContext(Dispatchers.Default) {
    try {
        val renderer = EpsRenderer(context)
        val bitmap = renderer.render(inputUri, scale.toInt(), bgColor)
        Result.Success(bitmap)
    } catch (e: Exception) {
        Result.Error(e, "Rendering failed")
    }
}
```

---

## Performance Optimization Tips

1. **Cache Rendered Pages**
   ```kotlin
   private val previewCache = mutableMapOf<Uri, Bitmap>()
   ```

2. **Downsample Large Files**
   ```kotlin
   val scale = if (fileSize > 10_000_000) 0.5f else 1.0f
   ```

3. **Use Background Thread**
   ```kotlin
   suspend fun renderPreview(...): Result<Bitmap> = withContext(Dispatchers.Default) { ... }
   ```

4. **Limit Memory Usage**
   ```kotlin
   bitmap.recycle() // After use
   System.gc() // Force garbage collection if needed
   ```

---

## Testing Renderer Integration

### Unit Test
```kotlin
@Test
fun `renderPreview returns valid bitmap`() = runBlocking {
    val result = epsRepository.renderPreview(testEpsUri, 1.0f)
    assertTrue(result.isSuccess())
    assertNotNull(result.getOrNull())
}
```

### Instrumented Test
```kotlin
@Test
fun `export creates output file`() {
    val outputUri = // ... create test output URI
    epsRepository.export(testEpsUri, outputUri, ExportFormat.PNG, 150)
    assertTrue(File(outputUri.path).exists())
}
```

---

## Troubleshooting

**Problem:** UnsatisfiedLinkError when loading native library
**Solution:** Ensure .so file is in correct arch folder (arm64-v8a, armeabi-v7a, etc.)

**Problem:** Renderer crashes on large files
**Solution:** Implement memory-efficient rendering with stream processing or downsampling

**Problem:** Exported file has wrong dimensions
**Solution:** Verify DPI calculation: `pixelSize = (dpi / 72.0) * pointSize`

---

## License Compliance

- **MuPDF**: AGPL v3 - Open source required
- **Ghostscript**: AGPL v3 - Open source required
- Consider dual-licensing or using commercial licenses

---

For more information:
- MuPDF: https://mupdf.com/
- Ghostscript: https://www.ghostscript.com/
- Artifex: https://www.artifex.com/

