package com.example.epsviewer.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.epsviewer.domain.model.EpsMetadata
import com.example.epsviewer.domain.model.ExportFormat
import com.example.epsviewer.domain.model.Result
import com.example.epsviewer.domain.repository.EpsRepository
import com.example.epsviewer.util.EpsParser
import com.example.epsviewer.util.EpsRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Implementation of EPS rendering repository.
 * Integrates EPS parser and renderer for actual file rendering.
 */
class EpsRepositoryImpl(private val context: Context) : EpsRepository {

    private val epsRenderer by lazy { EpsRenderer(context) }

    override suspend fun renderPreview(
        inputUri: Uri,
        scale: Float,
        bgColor: Int
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        try {
            Timber.d("Rendering preview for $inputUri at scale $scale")

            // Read and validate EPS file
            val inputStream = context.contentResolver.openInputStream(inputUri)
                ?: throw IOException("Cannot open input stream for $inputUri")

            // Validate it's an EPS file
            val isValid = inputStream.use { stream ->
                EpsParser.isValidEps(stream)
            }

            if (!isValid) {
                Timber.w("File is not a valid EPS file: $inputUri")
                return@withContext Result.Error(
                    Exception("Invalid EPS file"),
                    "This file does not appear to be a valid EPS/PostScript file"
                )
            }

            // Parse bounding box
            val boundingBox = context.contentResolver.openInputStream(inputUri)?.use { stream ->
                EpsParser.parseBoundingBox(stream)
            } ?: throw IOException("Cannot read EPS bounding box")

            Timber.d("EPS BoundingBox: ${boundingBox.width}x${boundingBox.height}")

            // Render the EPS content
            val bitmap = context.contentResolver.openInputStream(inputUri)?.use { stream ->
                epsRenderer.renderToBitmap(stream, boundingBox, scale)
            } ?: throw IOException("Cannot render EPS content")

            Timber.d("Successfully rendered EPS preview: ${bitmap.width}x${bitmap.height}")
            Result.Success(bitmap)

        } catch (e: Exception) {
            Timber.e(e, "Error rendering preview for $inputUri")
            Result.Error(e, "Failed to render EPS: ${e.message}")
        }
    }

    override suspend fun export(
        inputUri: Uri,
        outputUri: Uri,
        format: ExportFormat,
        dpi: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Exporting EPS from $inputUri to $outputUri as ${format.extension} at ${dpi}dpi")

            // Parse bounding box to get dimensions
            val boundingBox = context.contentResolver.openInputStream(inputUri)?.use { stream ->
                EpsParser.parseBoundingBox(stream)
            } ?: throw IOException("Cannot read EPS bounding box")

            // Calculate scale based on DPI (72 points = 1 inch)
            val scale = dpi / 72f

            // Render EPS to bitmap at requested DPI
            val bitmap = context.contentResolver.openInputStream(inputUri)?.use { stream ->
                epsRenderer.renderToBitmap(stream, boundingBox, scale)
            } ?: throw IOException("Cannot render EPS content")

            // Write to output
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                val success = when (format) {
                    ExportFormat.PNG -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    ExportFormat.JPG -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    ExportFormat.PDF -> {
                        // PDF export would require PDF library
                        // For now, save as high-quality PNG
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }

                if (!success) {
                    throw IOException("Failed to compress bitmap to ${format.extension}")
                }

                outputStream.flush()
            } ?: throw IOException("Cannot open output stream")

            bitmap.recycle()
            Timber.d("Successfully exported EPS to $outputUri as ${format.extension}")
            Result.Success(Unit)

        } catch (e: IOException) {
            Timber.e(e, "Error exporting EPS to $outputUri")
            Result.Error(e, "Failed to export: ${e.message}")
        }
    }

    override suspend fun getMetadata(inputUri: Uri): Result<EpsMetadata> =
        withContext(Dispatchers.IO) {
            try {
                val fileName = getFileName(inputUri)
                val fileSize = getFileSize(inputUri)

                // Parse EPS metadata
                val epsMetadata = context.contentResolver.openInputStream(inputUri)?.use { stream ->
                    EpsParser.parseMetadata(stream)
                } ?: emptyMap()

                // Parse bounding box for dimensions
                val boundingBox = context.contentResolver.openInputStream(inputUri)?.use { stream ->
                    EpsParser.parseBoundingBox(stream)
                } ?: EpsParser.EpsBoundingBox(0, 0, 612, 792)

                val metadata = EpsMetadata(
                    uri = inputUri,
                    fileName = fileName,
                    fileSize = fileSize,
                    pageCount = epsMetadata["pages"]?.toIntOrNull() ?: 1,
                    width = boundingBox.width,
                    height = boundingBox.height,
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = System.currentTimeMillis()
                )

                Timber.d("Loaded metadata for $fileName: ${boundingBox.width}x${boundingBox.height}, $fileSize bytes")
                Result.Success(metadata)
            } catch (e: Exception) {
                Timber.e(e, "Error getting metadata for $inputUri")
                Result.Error(e, "Failed to get metadata: ${e.message}")
            }
        }

    private fun getFileName(uri: Uri): String {
        var name = uri.lastPathSegment ?: "unknown.eps"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Could not query file name")
        }
        return name
    }

    override suspend fun batchExport(
        inputUris: List<Uri>,
        outputDirUri: Uri,
        format: ExportFormat,
        dpi: Int
    ): Result<Int> = withContext(Dispatchers.IO) {
        var successCount = 0

        for (uri in inputUris) {
            try {
                val fileName = uri.lastPathSegment?.substringBeforeLast(".") ?: "export"
                val outputFileName = "$fileName.${format.extension}"

                // In real implementation, create file in output directory
                val result = export(uri, uri, format, dpi)
                if (result.isSuccess()) {
                    successCount++
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to export $uri")
            }
        }

        Result.Success(successCount)
    }

    override suspend fun clearCache(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "eps_cache")
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing cache")
            Result.Error(e, "Failed to clear cache: ${e.message}")
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use {
                it.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

