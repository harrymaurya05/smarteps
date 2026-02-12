package com.example.epsviewer.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import com.example.epsviewer.domain.model.EpsMetadata
import com.example.epsviewer.domain.model.ExportFormat
import com.example.epsviewer.domain.model.Result

/**
 * Abstract repository for EPS rendering operations.
 * Implementations can swap between different rendering engines (MuPDF, Ghostscript, etc.)
 */
interface EpsRepository {
    /**
     * Render preview bitmap from EPS file
     * @param inputUri URI to EPS file
     * @param scale Scale factor for rendering (1.0 = 100%)
     * @param bgColor Background color (ARGB)
     * @return Result containing rendered Bitmap or Error
     */
    suspend fun renderPreview(inputUri: Uri, scale: Float = 1.0f, bgColor: Int = -1): Result<Bitmap>

    /**
     * Export EPS to specified format
     * @param inputUri URI to EPS file
     * @param outputUri URI to output file
     * @param format Target export format (PNG, JPG, PDF)
     * @param dpi Resolution in dots per inch (150-600)
     * @return Result containing success or Error
     */
    suspend fun export(
        inputUri: Uri,
        outputUri: Uri,
        format: ExportFormat,
        dpi: Int = 150
    ): Result<Unit>

    /**
     * Get metadata about EPS file
     * @param inputUri URI to EPS file
     * @return Result containing EpsMetadata or Error
     */
    suspend fun getMetadata(inputUri: Uri): Result<EpsMetadata>

    /**
     * Batch export multiple EPS files
     * @param inputUris List of URIs to EPS files
     * @param outputDirUri URI to output directory
     * @param format Target export format
     * @param dpi Resolution in dots per inch
     * @return Result with count of successful exports or Error
     */
    suspend fun batchExport(
        inputUris: List<Uri>,
        outputDirUri: Uri,
        format: ExportFormat,
        dpi: Int = 150
    ): Result<Int>

    /**
     * Clear rendering cache if any
     */
    suspend fun clearCache(): Result<Unit>
}

