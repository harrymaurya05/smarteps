package com.example.epsviewer.data

import android.content.Context
import android.content.SharedPreferences
import com.example.epsviewer.data.model.RecentFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentFilesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("recent_files", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val maxRecentFiles = 20

    fun addRecentFile(file: RecentFile) {
        val files = getRecentFiles().toMutableList()

        // Remove existing entry with same URI
        files.removeAll { it.uri == file.uri }

        // Add to beginning
        files.add(0, file.copy(lastOpenedTime = System.currentTimeMillis()))

        // Keep only max files
        val trimmedFiles = files.take(maxRecentFiles)

        // Save
        saveRecentFiles(trimmedFiles)
    }

    fun getRecentFiles(): List<RecentFile> {
        val json = prefs.getString("files", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<RecentFile>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearRecentFiles() {
        prefs.edit().remove("files").apply()
    }

    private fun saveRecentFiles(files: List<RecentFile>) {
        val json = gson.toJson(files)
        prefs.edit().putString("files", json).apply()
    }

    fun removeRecentFile(uri: String) {
        val files = getRecentFiles().toMutableList()
        files.removeAll { it.uri == uri }
        saveRecentFiles(files)
    }
}

