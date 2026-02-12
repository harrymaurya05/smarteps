package com.example.epsviewer.ui.viewer

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epsviewer.domain.model.EpsMetadata
import com.example.epsviewer.domain.model.ExportFormat
import com.example.epsviewer.domain.model.PremiumTier
import com.example.epsviewer.domain.model.Result
import com.example.epsviewer.domain.repository.BillingRepository
import com.example.epsviewer.domain.repository.EpsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ViewerUiState(
    val fileUri: Uri? = null,
    val metadata: EpsMetadata? = null,
    val preview: Bitmap? = null,
    val isLoading: Boolean = true,
    val loadingMessage: String? = null,
    val error: String? = null,
    val zoomLevel: Float = 1.0f,
    val premiumTier: PremiumTier = PremiumTier.FREE,
    val isExporting: Boolean = false,
    val exportProgress: Int = 0
)

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val epsRepository: EpsRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewerUiState())
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    fun loadFile(fileUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    fileUri = fileUri,
                    isLoading = true,
                    loadingMessage = "Loading EPS file...",
                    error = null
                ) }

                // Load metadata
                _uiState.update { it.copy(loadingMessage = "Parsing EPS metadata...") }
                val metadataResult = epsRepository.getMetadata(fileUri)
                if (!metadataResult.isSuccess()) {
                    throw Exception(metadataResult.exceptionOrNull()?.message ?: "Unknown error")
                }

                val metadata = metadataResult.getOrNull() ?: throw Exception("No metadata")
                _uiState.update { it.copy(metadata = metadata) }

                // Load preview with appropriate scale
                _uiState.update { it.copy(loadingMessage = "Rendering preview...") }
                Timber.d("Starting preview render for ${metadata.width}x${metadata.height}")

                val previewResult = epsRepository.renderPreview(fileUri, scale = 1.0f)
                if (!previewResult.isSuccess()) {
                    throw Exception(previewResult.exceptionOrNull()?.message ?: "Failed to render preview")
                }

                val preview = previewResult.getOrNull() ?: throw Exception("No preview bitmap")

                val tier = billingRepository.getPremiumTier()
                _uiState.update {
                    it.copy(
                        preview = preview,
                        isLoading = false,
                        loadingMessage = null,
                        premiumTier = tier
                    )
                }

                Timber.d("File loaded: ${fileUri.lastPathSegment}")
            } catch (e: Exception) {
                Timber.e(e, "Error loading file")
                _uiState.update { it.copy(
                    isLoading = false,
                    loadingMessage = null,
                    error = e.message
                ) }
            }
        }
    }

    fun export(outputUri: Uri, format: ExportFormat, dpi: Int) {
        viewModelScope.launch {
            try {
                val fileUri = _uiState.value.fileUri ?: throw Exception("No file loaded")
                _uiState.update { it.copy(isExporting = true, exportProgress = 0) }

                val result = epsRepository.export(fileUri, outputUri, format, dpi)

                if (result.isSuccess()) {
                    _uiState.update { it.copy(isExporting = false, exportProgress = 100) }
                    Timber.d("File exported successfully")
                } else {
                    throw Exception(result.exceptionOrNull()?.message ?: "Export failed")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error exporting file")
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun setZoomLevel(level: Float) {
        _uiState.update { it.copy(zoomLevel = level) }
    }

    fun resetZoom() {
        _uiState.update { it.copy(zoomLevel = 1.0f) }
    }

    fun fitToScreen() {
        // Zoom calculation would be done in UI layer based on screen size
        _uiState.update { it.copy(zoomLevel = 0.8f) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

