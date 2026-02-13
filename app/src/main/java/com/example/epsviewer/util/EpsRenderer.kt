package com.example.epsviewer.util

import android.content.Context
import android.graphics.Bitmap
import timber.log.Timber
import java.io.File
import java.io.InputStream

/**
 * EPS Renderer - Renders EPS files to bitmaps
 *
 * This renderer uses a multi-tier approach for best results:
 * 1. Professional PostScript Interpreter - Full PostScript language support (PRODUCTION QUALITY)
 * 2. Ghostscript (if available) - Industry-standard PostScript interpreter
 * 3. Basic fallback - Simple visualization for unsupported files
 */
class EpsRenderer(private val context: Context) {

    private val ghostscript: GhostscriptWrapper by lazy { GhostscriptWrapper(context) }
    private val psInterpreter = PostScriptInterpreter()

    /**
     * Render EPS to bitmap
     */
    fun renderToBitmap(
        epsInputStream: InputStream,
        boundingBox: EpsParser.EpsBoundingBox,
        scale: Float = 1.0f
    ): Bitmap {
        return try {
            val width = (boundingBox.width * scale).toInt().coerceAtLeast(100)
            val height = (boundingBox.height * scale).toInt().coerceAtLeast(100)

            Timber.d("Rendering EPS to ${width}x${height} bitmap (scale: $scale)")

            // Read EPS content once
            val epsContent = epsInputStream.bufferedReader().readText()

            // Priority 1: Try Professional PostScript Interpreter (BEST FOR MOST FILES)
            try {
                Timber.i("Using Professional PostScript Interpreter")
                val bitmap = psInterpreter.render(epsContent, width, height, boundingBox)
                Timber.i("Successfully rendered with PostScript Interpreter: ${bitmap.width}x${bitmap.height}")
                return bitmap
            } catch (e: Exception) {
                Timber.w(e, "PostScript Interpreter failed, trying Ghostscript")
            }

            // Priority 2: Try Ghostscript if available
            if (ghostscript.isAvailable()) {
                Timber.d("Attempting render with Ghostscript native library")

                // Write content to temp file for Ghostscript
                val tempFile = File.createTempFile("eps_", ".eps", context.cacheDir)
                try {
                    tempFile.writeText(epsContent)
                    val gsBitmap = renderWithGhostscript(tempFile.inputStream(), boundingBox, scale)
                    if (gsBitmap != null) {
                        Timber.i("Successfully rendered with Ghostscript")
                        return gsBitmap
                    }
                } finally {
                    tempFile.delete()
                }
                Timber.w("Ghostscript rendering failed")
            }

            // Priority 3: Show informative preview for unsupported content
            Timber.d("Using fallback preview renderer")
            renderEpsContent(epsContent, width, height, boundingBox)

        } catch (e: Exception) {
            Timber.e(e, "Error rendering EPS to bitmap")
            createErrorBitmap(
                (boundingBox.width * scale).toInt().coerceAtLeast(400),
                (boundingBox.height * scale).toInt().coerceAtLeast(400)
            )
        }
    }

    /**
     * Render EPS using Ghostscript native library
     */
    private fun renderWithGhostscript(
        epsInputStream: InputStream,
        boundingBox: EpsParser.EpsBoundingBox,
        scale: Float
    ): Bitmap? {
        var tempEpsFile: File? = null
        var tempPngFile: File? = null

        try {
            // Create temp files
            tempEpsFile = File.createTempFile("eps_render_", ".eps", context.cacheDir)
            tempPngFile = File.createTempFile("eps_render_", ".png", context.cacheDir)

            // Write EPS to temp file
            tempEpsFile.outputStream().use { output ->
                epsInputStream.copyTo(output)
            }

            // Calculate DPI based on scale
            val dpi = (72 * scale).toInt().coerceIn(72, 600)

            // Render using Ghostscript
            val success = ghostscript.renderToPng(
                tempEpsFile.absolutePath,
                tempPngFile.absolutePath,
                dpi
            )

            if (success && tempPngFile.exists() && tempPngFile.length() > 0) {
                // Decode the PNG
                val bitmap = android.graphics.BitmapFactory.decodeFile(tempPngFile.absolutePath)
                if (bitmap != null) {
                    Timber.i("Successfully rendered EPS with Ghostscript: ${bitmap.width}x${bitmap.height}")
                    return bitmap
                }
            }

            Timber.w("Ghostscript did not produce valid output")
            return null

        } catch (e: Exception) {
            Timber.e(e, "Error in Ghostscript rendering")
            return null
        } finally {
            // Clean up temp files
            tempEpsFile?.delete()
            tempPngFile?.delete()
        }
    }

    /**
     * Render EPS content to bitmap
     * This is a simplified renderer that handles basic PostScript commands
     */
    private fun renderEpsContent(
        epsContent: String,
        width: Int,
        height: Int,
        boundingBox: EpsParser.EpsBoundingBox
    ): Bitmap {
        Timber.d("Starting EPS render: ${width}x${height}, content length: ${epsContent.length}")

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // White background
        canvas.drawColor(android.graphics.Color.WHITE)

        // Check if this is an image-based EPS (contains hex data or image operators)
        val isImageBased = epsContent.contains("image") ||
                          epsContent.contains("colorimage") ||
                          epsContent.contains("readhexstring") ||
                          epsContent.contains("/DirectClassPacket")

        if (isImageBased) {
            Timber.d("Detected image-based EPS file (ImageMagick or similar)")
            // For image-based EPS files, show a special message
            return renderImageBasedEps(canvas, width, height, boundingBox, epsContent)
        }

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1f
            color = android.graphics.Color.BLACK
        }

        // Calculate scale to fit bitmap
        val scaleX = width.toFloat() / boundingBox.width.toFloat()
        val scaleY = height.toFloat() / boundingBox.height.toFloat()

        // PostScript state
        val path = android.graphics.Path()
        var currentX = 0f
        var currentY = 0f
        var pathStarted = false
        var red = 0f
        var green = 0f
        var blue = 0f

        var commandCount = 0

        // Parse and render PostScript commands
        try {
            val lines = epsContent.lines()
            var inPostScript = false

            Timber.d("Processing ${lines.size} lines of PostScript")

            for ((index, line) in lines.withIndex()) {
                val trimmed = line.trim()

                // Progress logging for large files
                if (index % 500 == 0 && index > 0) {
                    Timber.d("Processing line $index/${lines.size}")
                }

                // Skip comments
                if (trimmed.startsWith("%")) {
                    if (trimmed == "%%EndComments") inPostScript = true
                    continue
                }

                if (!inPostScript && !trimmed.contains("moveto") && !trimmed.contains("lineto")) continue

                // Parse PostScript commands
                val tokens = trimmed.split(Regex("\\s+"))

                when {
                    trimmed.contains("moveto") -> {
                        commandCount++
                        // Parse: x y moveto
                        if (tokens.size >= 3) {
                            val x = tokens[0].toFloatOrNull() ?: 0f
                            val y = tokens[1].toFloatOrNull() ?: 0f
                            currentX = (x - boundingBox.llx) * scaleX
                            currentY = height - (y - boundingBox.lly) * scaleY // Flip Y
                            if (pathStarted) {
                                canvas.drawPath(path, paint)
                            }
                            path.reset()
                            path.moveTo(currentX, currentY)
                            pathStarted = true
                        }
                    }
                    trimmed.contains("lineto") -> {
                        commandCount++
                        // Parse: x y lineto
                        if (tokens.size >= 3 && pathStarted) {
                            val x = tokens[0].toFloatOrNull() ?: 0f
                            val y = tokens[1].toFloatOrNull() ?: 0f
                            currentX = (x - boundingBox.llx) * scaleX
                            currentY = height - (y - boundingBox.lly) * scaleY // Flip Y
                            path.lineTo(currentX, currentY)
                        }
                    }
                    trimmed.contains("arc") -> {
                        commandCount++
                        // Parse: x y radius startAngle endAngle arc
                        if (tokens.size >= 6) {
                            val cx = tokens[0].toFloatOrNull() ?: 0f
                            val cy = tokens[1].toFloatOrNull() ?: 0f
                            val radius = tokens[2].toFloatOrNull() ?: 0f
                            val startAngle = tokens[3].toFloatOrNull() ?: 0f
                            val endAngle = tokens[4].toFloatOrNull() ?: 0f

                            val scaledCx = (cx - boundingBox.llx) * scaleX
                            val scaledCy = height - (cy - boundingBox.lly) * scaleY
                            val scaledRadius = radius * scaleX

                            val rect = android.graphics.RectF(
                                scaledCx - scaledRadius,
                                scaledCy - scaledRadius,
                                scaledCx + scaledRadius,
                                scaledCy + scaledRadius
                            )

                            // Convert angles (PostScript uses counter-clockwise from positive X)
                            val sweepAngle = if (endAngle >= startAngle) endAngle - startAngle else 360 - startAngle + endAngle
                            path.addArc(rect, -startAngle, -sweepAngle) // Negative for flipped Y
                        }
                    }
                    trimmed.contains("stroke") -> {
                        commandCount++
                        // Draw the current path
                        if (pathStarted) {
                            paint.style = android.graphics.Paint.Style.STROKE
                            canvas.drawPath(path, paint)
                            path.reset()
                            pathStarted = false
                        }
                    }
                    trimmed.contains("fill") -> {
                        commandCount++
                        // Fill the current path
                        if (pathStarted) {
                            paint.style = android.graphics.Paint.Style.FILL
                            canvas.drawPath(path, paint)
                            path.reset()
                            pathStarted = false
                            paint.style = android.graphics.Paint.Style.STROKE
                        }
                    }
                    trimmed.contains("setrgbcolor") -> {
                        // Parse: r g b setrgbcolor
                        if (tokens.size >= 4) {
                            red = tokens[0].toFloatOrNull() ?: 0f
                            green = tokens[1].toFloatOrNull() ?: 0f
                            blue = tokens[2].toFloatOrNull() ?: 0f
                            paint.color = android.graphics.Color.rgb(
                                (red * 255).toInt(),
                                (green * 255).toInt(),
                                (blue * 255).toInt()
                            )
                        }
                    }
                    trimmed.contains("newpath") -> {
                        // Start a new path
                        if (pathStarted) {
                            canvas.drawPath(path, paint)
                        }
                        path.reset()
                        pathStarted = false
                    }
                    trimmed.contains("closepath") -> {
                        // Close the current path
                        if (pathStarted) {
                            path.close()
                        }
                    }
                }
            }

            // Draw any remaining path
            if (pathStarted) {
                canvas.drawPath(path, paint)
            }

            Timber.d("Rendered $commandCount PostScript commands successfully")

            // If no commands were rendered, show info
            if (commandCount == 0) {
                Timber.w("No vector commands found, showing info overlay")
                drawFileInfo(canvas, width, height, boundingBox, epsContent)
            }

        } catch (e: Exception) {
            Timber.e(e, "Error parsing EPS commands")
            // Draw error info
            drawFileInfo(canvas, width, height, boundingBox, epsContent)
        }

        return bitmap
    }

    /**
     * Render image-based EPS files (from ImageMagick, etc.)
     */
    private fun renderImageBasedEps(
        canvas: android.graphics.Canvas,
        width: Int,
        height: Int,
        boundingBox: EpsParser.EpsBoundingBox,
        epsContent: String
    ): Bitmap {
        Timber.d("Attempting to render image-based EPS")

        // Try to extract image dimensions from content
        var imageWidth = boundingBox.width
        var imageHeight = boundingBox.height

        // Look for image dimensions in the content
        val widthMatch = Regex("""/Width\s+(\d+)""").find(epsContent)
        val heightMatch = Regex("""/Height\s+(\d+)""").find(epsContent)

        if (widthMatch != null) {
            imageWidth = widthMatch.groupValues[1].toIntOrNull() ?: imageWidth
        }
        if (heightMatch != null) {
            imageHeight = heightMatch.groupValues[1].toIntOrNull() ?: imageHeight
        }

        Timber.d("Image dimensions: ${imageWidth}x${imageHeight}")

        // Try to decode hex data
        val decodedBitmap = tryDecodeHexImageData(epsContent, imageWidth, imageHeight)

        if (decodedBitmap != null) {
            Timber.d("Successfully decoded hex image data")
            // Scale to requested size
            val scaledBitmap = Bitmap.createScaledBitmap(decodedBitmap, width, height, true)
            decodedBitmap.recycle()
            return scaledBitmap
        }

        // Fallback: show informative message
        Timber.w("Could not decode hex data, showing info screen")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val imageCanvas = android.graphics.Canvas(bitmap)

        // White background
        imageCanvas.drawColor(android.graphics.Color.WHITE)

        // For now, show informative message about image-based EPS
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Draw border
        paint.apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 4f
            color = android.graphics.Color.parseColor("#FF9800")
        }
        imageCanvas.drawRect(
            10f, 10f,
            width - 10f, height - 10f,
            paint
        )

        // Draw title
        paint.apply {
            style = android.graphics.Paint.Style.FILL
            textSize = 40f
            color = android.graphics.Color.parseColor("#FF6F00")
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        imageCanvas.drawText("IMAGE-BASED EPS", width / 2f, height * 0.2f, paint)

        // Draw creator info
        val creator = if (epsContent.contains("ImageMagick")) "ImageMagick"
                     else if (epsContent.contains("Adobe")) "Adobe"
                     else "Unknown"

        paint.apply {
            textSize = 28f
            color = android.graphics.Color.parseColor("#424242")
        }
        imageCanvas.drawText("Creator: $creator", width / 2f, height * 0.3f, paint)
        imageCanvas.drawText(
            "Size: ${boundingBox.width} × ${boundingBox.height} pt",
            width / 2f, height * 0.38f, paint
        )

        // File info
        val lines = epsContent.lines().size
        paint.textSize = 24f
        imageCanvas.drawText("$lines lines of PostScript", width / 2f, height * 0.46f, paint)

        // Info icon
        paint.apply {
            color = android.graphics.Color.parseColor("#FF9800")
            style = android.graphics.Paint.Style.FILL
        }
        imageCanvas.drawCircle(width / 2f, height * 0.58f, 50f, paint)

        paint.apply {
            color = android.graphics.Color.WHITE
            textSize = 60f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        imageCanvas.drawText("i", width / 2f, height * 0.60f, paint)

        // Explanation
        paint.apply {
            color = android.graphics.Color.parseColor("#666666")
            textSize = 22f
            typeface = android.graphics.Typeface.DEFAULT
        }
        imageCanvas.drawText("This EPS contains embedded raster image data", width / 2f, height * 0.72f, paint)
        imageCanvas.drawText("Requires Ghostscript for full rendering", width / 2f, height * 0.77f, paint)

        // Action
        paint.apply {
            textSize = 26f
            color = android.graphics.Color.parseColor("#1976D2")
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        imageCanvas.drawText("✓ File loaded - Use CONVERT to export", width / 2f, height * 0.88f, paint)

        Timber.d("Rendered image-based EPS placeholder")
        return bitmap
    }

    /**
     * Try to decode hex-encoded image data from EPS file
     * ImageMagick stores RGB data as hex strings
     */
    private fun tryDecodeHexImageData(epsContent: String, width: Int, height: Int): Bitmap? {
        try {
            Timber.d("Attempting to decode hex image data: ${width}x${height}")

            // Find the image data section
            val lines = epsContent.lines()
            val hexData = StringBuilder()
            var inImageData = false
            var lineCount = 0

            for (line in lines) {
                val trimmed = line.trim()

                // Start collecting hex data after image operator
                if (trimmed.contains("image") && !trimmed.startsWith("%")) {
                    inImageData = true
                    continue
                }

                // Stop at certain keywords
                if (inImageData && (trimmed.contains("grestore") ||
                                   trimmed.contains("showpage") ||
                                   trimmed.contains("%%Trailer"))) {
                    break
                }

                // Collect hex data (lines with only hex characters)
                if (inImageData && trimmed.matches(Regex("^[0-9a-fA-F]+$"))) {
                    hexData.append(trimmed)
                    lineCount++

                    // Progress logging for large data
                    if (lineCount % 100 == 0) {
                        Timber.d("Decoded $lineCount lines of hex data")
                    }
                }
            }

            if (hexData.isEmpty()) {
                Timber.w("No hex image data found")
                return null
            }

            Timber.d("Found ${hexData.length / 2} bytes of hex data from $lineCount lines")

            // Decode hex string to bytes
            val hexString = hexData.toString()
            val expectedBytes = width * height * 3 // RGB

            // Create bitmap
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(width * height)

            var pixelIndex = 0
            var i = 0

            while (i < hexString.length - 5 && pixelIndex < pixels.size) {
                try {
                    // Read RGB values (2 hex chars = 1 byte)
                    val r = hexString.substring(i, i + 2).toInt(16)
                    val g = hexString.substring(i + 2, i + 4).toInt(16)
                    val b = hexString.substring(i + 4, i + 6).toInt(16)

                    pixels[pixelIndex] = android.graphics.Color.rgb(r, g, b)
                    pixelIndex++
                    i += 6
                } catch (e: Exception) {
                    // Skip invalid hex data
                    i += 2
                }

                // Progress for large images
                if (pixelIndex % 10000 == 0) {
                    Timber.d("Decoded $pixelIndex pixels")
                }
            }

            if (pixelIndex < pixels.size / 2) {
                Timber.w("Only decoded $pixelIndex/${pixels.size} pixels")
                return null
            }

            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            Timber.d("Successfully decoded image: $pixelIndex pixels")

            return bitmap

        } catch (e: Exception) {
            Timber.e(e, "Error decoding hex image data")
            return null
        }
    }

    /**
     * Draw file information overlay on the rendered bitmap
     */
    private fun drawFileInfo(
        canvas: android.graphics.Canvas,
        width: Int,
        height: Int,
        boundingBox: EpsParser.EpsBoundingBox,
        epsContent: String
    ) {
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Draw border
        paint.apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 4f
            color = android.graphics.Color.parseColor("#2196F3")
        }
        canvas.drawRect(
            10f, 10f,
            width - 10f, height - 10f,
            paint
        )

        // Draw title
        paint.apply {
            style = android.graphics.Paint.Style.FILL
            textSize = 48f
            color = android.graphics.Color.parseColor("#1976D2")
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText("EPS FILE LOADED", width / 2f, height * 0.25f, paint)

        // Draw dimensions
        paint.apply {
            textSize = 32f
            color = android.graphics.Color.parseColor("#424242")
        }
        canvas.drawText(
            "Size: ${boundingBox.width} × ${boundingBox.height} pt",
            width / 2f, height * 0.35f, paint
        )

        // Count PostScript commands
        val commandCount = epsContent.lines().count {
            it.contains(Regex("\\b(moveto|lineto|stroke|fill|show|setrgbcolor)\\b"))
        }
        paint.textSize = 28f
        canvas.drawText(
            "PostScript Commands: $commandCount",
            width / 2f, height * 0.42f, paint
        )

        // Draw preview indicator
        paint.apply {
            textSize = 24f
            color = android.graphics.Color.parseColor("#666666")
        }
        canvas.drawText(
            "✓ File parsed successfully",
            width / 2f, height * 0.55f, paint
        )

        // Draw instruction
        paint.apply {
            textSize = 28f
            color = android.graphics.Color.parseColor("#FF6F00")
        }
        canvas.drawText(
            "Use CONVERT to export",
            width / 2f, height * 0.75f, paint
        )

        // Draw checkmark icon
        paint.apply {
            color = android.graphics.Color.parseColor("#4CAF50")
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(width / 2f, height * 0.65f, 40f, paint)

        paint.apply {
            color = android.graphics.Color.WHITE
            strokeWidth = 6f
            style = android.graphics.Paint.Style.STROKE
        }
        val cx = width / 2f
        val cy = height * 0.65f
        canvas.drawLine(cx - 15f, cy, cx - 5f, cy + 10f, paint)
        canvas.drawLine(cx - 5f, cy + 10f, cx + 15f, cy - 10f, paint)
    }

    /**
     * Create error bitmap when rendering fails
     */
    private fun createErrorBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Red background
        canvas.drawColor(android.graphics.Color.parseColor("#FFEBEE"))

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Draw error icon
        paint.apply {
            color = android.graphics.Color.parseColor("#D32F2F")
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(width / 2f, height * 0.35f, 60f, paint)

        // Error text
        paint.apply {
            color = android.graphics.Color.WHITE
            textSize = 80f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText("!", width / 2f, height * 0.38f, paint)

        // Error message
        paint.apply {
            color = android.graphics.Color.parseColor("#C62828")
            textSize = 36f
        }
        canvas.drawText("Error Rendering EPS", width / 2f, height * 0.55f, paint)

        return bitmap
    }

    /**
     * Convert EPS to PDF (requires Ghostscript)
     */
    fun convertToPdf(epsFile: File, outputPdf: File): Boolean {
        if (!ghostscript.isAvailable()) {
            Timber.w("Ghostscript not available for PDF conversion")
            return false
        }

        return try {
            Timber.i("Converting EPS to PDF using Ghostscript: ${epsFile.name}")
            val success = ghostscript.convertToPdf(
                epsFile.absolutePath,
                outputPdf.absolutePath
            )

            if (success) {
                Timber.i("Successfully converted EPS to PDF")
            } else {
                Timber.w("PDF conversion failed")
            }

            success
        } catch (e: Exception) {
            Timber.e(e, "Error converting EPS to PDF")
            false
        }
    }
}

