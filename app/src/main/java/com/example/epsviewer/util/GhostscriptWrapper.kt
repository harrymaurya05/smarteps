package com.example.epsviewer.util

import android.content.Context
import timber.log.Timber
import java.io.File

/**
 * JNI Wrapper for Ghostscript native library
 *
 * This class provides a bridge to the native Ghostscript library for rendering
 * EPS files to raster formats (PNG, JPEG, etc.) and converting to PDF.
 *
 * Native library must be present: libgs.so in jniLibs/
 *
 * @see <a href="https://www.ghostscript.com/">Ghostscript Official Site</a>
 */
class GhostscriptWrapper(private val context: Context) {

    private var isLibraryLoaded = false

    init {
        try {
            System.loadLibrary("gs")
            isLibraryLoaded = true
            Timber.i("Ghostscript native library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Timber.w(e, "Ghostscript native library not available. Install libgs.so")
            isLibraryLoaded = false
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error loading Ghostscript library")
            isLibraryLoaded = false
        }
    }

    /**
     * Check if Ghostscript is available
     */
    fun isAvailable(): Boolean = isLibraryLoaded

    /**
     * Get Ghostscript version
     * @return Version string (e.g., "10.02.1") or null if unavailable
     */
    fun getVersion(): String? {
        if (!isLibraryLoaded) return null
        return try {
            nativeGetVersion()
        } catch (e: Exception) {
            Timber.e(e, "Error getting Ghostscript version")
            null
        }
    }

    /**
     * Render EPS file to PNG
     *
     * @param inputPath Absolute path to EPS file
     * @param outputPath Absolute path to output PNG file
     * @param dpi Resolution in dots per inch (default: 150)
     * @return True if successful, false otherwise
     */
    fun renderToPng(inputPath: String, outputPath: String, dpi: Int = 150): Boolean {
        if (!isLibraryLoaded) {
            Timber.w("Ghostscript not available for PNG rendering")
            return false
        }

        return try {
            Timber.d("Rendering EPS to PNG: $inputPath -> $outputPath at ${dpi}dpi")
            val result = nativeRenderToPng(inputPath, outputPath, dpi)
            if (result) {
                Timber.i("Successfully rendered EPS to PNG")
            } else {
                Timber.w("Ghostscript rendering failed")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Error rendering EPS to PNG")
            false
        }
    }

    /**
     * Render EPS file to JPEG
     *
     * @param inputPath Absolute path to EPS file
     * @param outputPath Absolute path to output JPEG file
     * @param dpi Resolution in dots per inch (default: 150)
     * @param quality JPEG quality 0-100 (default: 90)
     * @return True if successful, false otherwise
     */
    fun renderToJpeg(
        inputPath: String,
        outputPath: String,
        dpi: Int = 150,
        quality: Int = 90
    ): Boolean {
        if (!isLibraryLoaded) {
            Timber.w("Ghostscript not available for JPEG rendering")
            return false
        }

        return try {
            Timber.d("Rendering EPS to JPEG: $inputPath -> $outputPath at ${dpi}dpi, quality=$quality")
            val result = nativeRenderToJpeg(inputPath, outputPath, dpi, quality)
            if (result) {
                Timber.i("Successfully rendered EPS to JPEG")
            } else {
                Timber.w("Ghostscript JPEG rendering failed")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Error rendering EPS to JPEG")
            false
        }
    }

    /**
     * Convert EPS file to PDF
     *
     * @param inputPath Absolute path to EPS file
     * @param outputPath Absolute path to output PDF file
     * @return True if successful, false otherwise
     */
    fun convertToPdf(inputPath: String, outputPath: String): Boolean {
        if (!isLibraryLoaded) {
            Timber.w("Ghostscript not available for PDF conversion")
            return false
        }

        return try {
            Timber.d("Converting EPS to PDF: $inputPath -> $outputPath")
            val result = nativeConvertToPdf(inputPath, outputPath)
            if (result) {
                Timber.i("Successfully converted EPS to PDF")
            } else {
                Timber.w("Ghostscript PDF conversion failed")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Error converting EPS to PDF")
            false
        }
    }

    /**
     * Render EPS with custom arguments
     *
     * Advanced usage: Provide custom Ghostscript command-line arguments
     *
     * @param args Array of Ghostscript arguments (e.g., ["-sDEVICE=png16m", "-r150", "-o", "/path/out.png", "/path/in.eps"])
     * @return True if successful, false otherwise
     */
    fun executeCustom(args: Array<String>): Boolean {
        if (!isLibraryLoaded) {
            Timber.w("Ghostscript not available for custom execution")
            return false
        }

        return try {
            Timber.d("Executing custom Ghostscript command: ${args.joinToString(" ")}")
            val result = nativeExecute(args)
            if (result) {
                Timber.i("Ghostscript custom command succeeded")
            } else {
                Timber.w("Ghostscript custom command failed")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Error executing custom Ghostscript command")
            false
        }
    }

    /**
     * Get bounding box from EPS file
     *
     * @param inputPath Absolute path to EPS file
     * @return BoundingBox array [llx, lly, urx, ury] or null if unavailable
     */
    fun getBoundingBox(inputPath: String): IntArray? {
        if (!isLibraryLoaded) {
            Timber.w("Ghostscript not available for bounding box extraction")
            return null
        }

        return try {
            nativeGetBoundingBox(inputPath)
        } catch (e: Exception) {
            Timber.e(e, "Error getting bounding box from EPS")
            null
        }
    }

    // Native methods (implemented in C++)
    private external fun nativeGetVersion(): String
    private external fun nativeRenderToPng(inputPath: String, outputPath: String, dpi: Int): Boolean
    private external fun nativeRenderToJpeg(inputPath: String, outputPath: String, dpi: Int, quality: Int): Boolean
    private external fun nativeConvertToPdf(inputPath: String, outputPath: String): Boolean
    private external fun nativeExecute(args: Array<String>): Boolean
    private external fun nativeGetBoundingBox(inputPath: String): IntArray?

    companion object {
        /**
         * Check if Ghostscript library exists
         */
        fun isGhostscriptInstalled(context: Context): Boolean {
            return try {
                System.loadLibrary("gs")
                true
            } catch (e: UnsatisfiedLinkError) {
                false
            }
        }
    }
}

