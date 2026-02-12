package com.example.epsviewer.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epsviewer.domain.model.EpsMetadata
import com.example.epsviewer.domain.model.PremiumTier
import com.example.epsviewer.domain.repository.BillingRepository
import com.example.epsviewer.domain.repository.EpsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class HomeUiState(
    val recentFiles: List<EpsMetadata> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val premiumTier: PremiumTier = PremiumTier.FREE,
    val adVisible: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val epsRepository: EpsRepository,
    private val billingRepository: BillingRepository,
    private val recentFilesManager: com.example.epsviewer.data.RecentFilesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Load billing state
                billingRepository.initialize()
                val tier = billingRepository.getPremiumTier()
                _uiState.update {
                    it.copy(
                        premiumTier = tier,
                        adVisible = tier == PremiumTier.FREE
                    )
                }

                // Load recent files from persistent storage
                val recentFiles = recentFilesManager.getRecentFiles()
                _uiState.update { it.copy(recentFiles = emptyList()) } // Will update UI separately

                Timber.d("Home screen loaded with tier: $tier")
            } catch (e: Exception) {
                Timber.e(e, "Error loading initial data")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun openFile(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Load metadata to validate file
                val result = epsRepository.getMetadata(uri)
                if (result.isSuccess()) {
                    Timber.d("File opened: ${uri.lastPathSegment}")
                    // Navigation will be handled by activity
                } else {
                    _uiState.update {
                        it.copy(error = "Failed to open file: ${result.exceptionOrNull()?.message}")
                    }
                }

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Timber.e(e, "Error opening file")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun addRecentFile(file: com.example.epsviewer.data.model.RecentFile) {
        viewModelScope.launch {
            recentFilesManager.addRecentFile(file)
            Timber.d("Added to recent files: ${file.fileName}")
        }
    }

    fun getRecentFiles(): List<com.example.epsviewer.data.model.RecentFile> {
        return recentFilesManager.getRecentFiles()
    }
}

