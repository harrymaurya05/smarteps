package com.example.epsviewer.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epsviewer.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class PremiumUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val purchaseSuccess: Boolean = false
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    fun purchaseMonthly() {
        purchase("com.example.epsviewer.monthly")
    }

    fun purchaseAnnual() {
        purchase("com.example.epsviewer.annual")
    }

    fun purchaseLifetime() {
        purchase("com.example.epsviewer.lifetime")
    }

    private fun purchase(productId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val success = billingRepository.launchPurchaseFlow(productId)

                if (success) {
                    _uiState.update { it.copy(isLoading = false, purchaseSuccess = true) }
                    Timber.d("Purchase successful: $productId")
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Purchase failed") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error processing purchase")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

