package com.example.epsviewer.domain.model

import android.net.Uri

/**
 * Metadata about an EPS file
 */
data class EpsMetadata(
    val uri: Uri,
    val fileName: String,
    val fileSize: Long,
    val pageCount: Int = 1,
    val width: Int = 0,
    val height: Int = 0,
    val createdAt: Long = 0,
    val modifiedAt: Long = 0
)

