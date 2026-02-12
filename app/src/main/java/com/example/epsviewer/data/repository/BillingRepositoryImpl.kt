package com.example.epsviewer.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.epsviewer.domain.model.BillingState
import com.example.epsviewer.domain.model.PremiumTier
import com.example.epsviewer.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

/**
 * Implementation of billing repository using Google Play Billing v6+
 * This is a simplified version that manages local state.
 * In production, integrate with Google Play Billing Client for actual purchases.
 */
class BillingRepositoryImpl(private val context: Context) : BillingRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)

    private val _billingStateFlow = MutableStateFlow(loadInitialState())
    override val billingStateFlow: Flow<BillingState> = _billingStateFlow

    override suspend fun initialize(): Boolean {
        return try {
            // TODO: Initialize Google Play Billing Client
            // For now, just mark as ready
            _billingStateFlow.value = _billingStateFlow.value.copy(isReady = true)
            Timber.d("Billing repository initialized")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize billing")
            _billingStateFlow.value = _billingStateFlow.value.copy(
                isReady = false,
                error = e.message
            )
            false
        }
    }

    override suspend fun isPremium(): Boolean {
        val tier = getPremiumTier()
        return tier != PremiumTier.FREE
    }

    override suspend fun getPremiumTier(): PremiumTier {
        return when {
            prefs.getBoolean("is_lifetime", false) -> PremiumTier.LIFETIME_SUBSCRIBER
            prefs.getBoolean("is_annual", false) -> PremiumTier.ANNUAL_SUBSCRIBER
            prefs.getBoolean("is_monthly", false) -> PremiumTier.MONTHLY_SUBSCRIBER
            else -> PremiumTier.FREE
        }
    }

    override suspend fun launchPurchaseFlow(productId: String): Boolean {
        return try {
            // TODO: Launch actual Google Play purchase flow
            // For testing, this will succeed
            when {
                productId.contains("monthly") -> {
                    prefs.edit().putBoolean("is_monthly", true).apply()
                }
                productId.contains("annual") -> {
                    prefs.edit().putBoolean("is_annual", true).apply()
                }
                productId.contains("lifetime") -> {
                    prefs.edit().putBoolean("is_lifetime", true).apply()
                }
            }
            updateBillingState()
            Timber.d("Purchase successful for $productId")
            true
        } catch (e: Exception) {
            Timber.e(e, "Purchase failed for $productId")
            false
        }
    }

    override suspend fun hasRewardedAdUsed(): Boolean {
        return prefs.getBoolean("rewarded_ad_used", false)
    }

    override suspend fun markRewardedAdUsed(): Boolean {
        return try {
            prefs.edit().putBoolean("rewarded_ad_used", true).apply()
            Timber.d("Rewarded ad marked as used")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to mark rewarded ad as used")
            false
        }
    }

    override suspend fun cleanup() {
        // Clean up any resources
        Timber.d("Billing repository cleaned up")
    }

    private fun loadInitialState(): BillingState {
        val tier = when {
            prefs.getBoolean("is_lifetime", false) -> PremiumTier.LIFETIME_SUBSCRIBER
            prefs.getBoolean("is_annual", false) -> PremiumTier.ANNUAL_SUBSCRIBER
            prefs.getBoolean("is_monthly", false) -> PremiumTier.MONTHLY_SUBSCRIBER
            else -> PremiumTier.FREE
        }
        return BillingState(tier = tier, isReady = false)
    }

    private fun updateBillingState() {
        val tier = when {
            prefs.getBoolean("is_lifetime", false) -> PremiumTier.LIFETIME_SUBSCRIBER
            prefs.getBoolean("is_annual", false) -> PremiumTier.ANNUAL_SUBSCRIBER
            prefs.getBoolean("is_monthly", false) -> PremiumTier.MONTHLY_SUBSCRIBER
            else -> PremiumTier.FREE
        }
        _billingStateFlow.value = _billingStateFlow.value.copy(tier = tier)
    }
}

