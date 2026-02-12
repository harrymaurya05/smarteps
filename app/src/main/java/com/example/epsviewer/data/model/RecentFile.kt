 package com.example.epsviewer.data.model

data class RecentFile(
    val uri: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val lastOpenedTime: Long,
    val thumbnailPath: String? = null
)

