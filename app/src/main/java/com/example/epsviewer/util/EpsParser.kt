package com.example.epsviewer.util

import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * EPS File Parser - Extracts metadata and bounding box from EPS files
 */
@Suppress("unused")
object EpsParser {

    data class EpsBoundingBox(
        val llx: Int,  // Lower-left X
        val lly: Int,  // Lower-left Y
        val urx: Int,  // Upper-right X
        val ury: Int   // Upper-right Y
    ) {
        val width: Int get() = urx - llx
        val height: Int get() = ury - lly
    }

    /**
     * Parse EPS file to extract bounding box
     * Looks for %%BoundingBox: comment in EPS header
     */
    fun parseBoundingBox(inputStream: InputStream): EpsBoundingBox {
        try {
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                // Read first 100 lines to find bounding box
                for (i in 0 until 100) {
                    val line = reader.readLine() ?: break

                    // Look for %%BoundingBox: llx lly urx ury
                    if (line.startsWith("%%BoundingBox:")) {
                        val parts = line.substringAfter("%%BoundingBox:")
                            .trim()
                            .split(Regex("\\s+"))

                        if (parts.size >= 4 && parts[0] != "(atend)") {
                            try {
                                val llx = parts[0].toInt()
                                val lly = parts[1].toInt()
                                val urx = parts[2].toInt()
                                val ury = parts[3].toInt()

                                Timber.d("Found BoundingBox: $llx $lly $urx $ury")
                                return EpsBoundingBox(llx, lly, urx, ury)
                            } catch (e: NumberFormatException) {
                                Timber.w("Invalid BoundingBox format: $line")
                            }
                        }
                    }

                    // Also check for HiResBoundingBox (more precise)
                    if (line.startsWith("%%HiResBoundingBox:")) {
                        val parts = line.substringAfter("%%HiResBoundingBox:")
                            .trim()
                            .split(Regex("\\s+"))

                        if (parts.size >= 4) {
                            try {
                                val llx = parts[0].toDouble().toInt()
                                val lly = parts[1].toDouble().toInt()
                                val urx = parts[2].toDouble().toInt()
                                val ury = parts[3].toDouble().toInt()

                                Timber.d("Found HiResBoundingBox: $llx $lly $urx $ury")
                                return EpsBoundingBox(llx, lly, urx, ury)
                            } catch (e: NumberFormatException) {
                                Timber.w("Invalid HiResBoundingBox format: $line")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing EPS bounding box")
        }

        // Default bounding box if not found (US Letter size at 72dpi)
        Timber.w("BoundingBox not found, using default: 612x792")
        return EpsBoundingBox(0, 0, 612, 792)
    }

    /**
     * Extract metadata from EPS file header
     */
    fun parseMetadata(inputStream: InputStream): Map<String, String> {
        val metadata = mutableMapOf<String, String>()

        try {
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                // Read first 50 lines of header
                for (i in 0 until 50) {
                    val line = reader.readLine() ?: break

                    // Parse DSC comments
                    when {
                        line.startsWith("%%Title:") -> {
                            metadata["title"] = line.substringAfter("%%Title:").trim()
                        }
                        line.startsWith("%%Creator:") -> {
                            metadata["creator"] = line.substringAfter("%%Creator:").trim()
                        }
                        line.startsWith("%%CreationDate:") -> {
                            metadata["creationDate"] = line.substringAfter("%%CreationDate:").trim()
                        }
                        line.startsWith("%%Pages:") -> {
                            metadata["pages"] = line.substringAfter("%%Pages:").trim()
                        }
                        line.startsWith("%%LanguageLevel:") -> {
                            metadata["languageLevel"] = line.substringAfter("%%LanguageLevel:").trim()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing EPS metadata")
        }

        return metadata
    }

    /**
     * Validate if file is a valid EPS file
     */
    fun isValidEps(inputStream: InputStream): Boolean {
        try {
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val firstLine = reader.readLine() ?: return false
                // EPS files must start with %!PS-Adobe-
                return firstLine.startsWith("%!PS-Adobe-") ||
                       firstLine.startsWith("%!PS") ||
                       firstLine.startsWith("\u00C5\u00D0\u00D3\u00C3") // Binary EPS header
            }
        } catch (e: Exception) {
            Timber.e(e, "Error validating EPS file")
            return false
        }
    }
}

