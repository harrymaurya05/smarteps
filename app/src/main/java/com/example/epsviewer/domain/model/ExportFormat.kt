package com.example.epsviewer.domain.model

/**
 * Export format types for EPS conversion
 */
enum class ExportFormat(val extension: String, val mimeType: String) {
    PNG("png", "image/png"),
    JPG("jpg", "image/jpeg"),
    PDF("pdf", "application/pdf");

    override fun toString(): String = extension.uppercase()
}

