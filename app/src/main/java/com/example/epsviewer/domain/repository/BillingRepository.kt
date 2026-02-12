package com.example.epsviewer.domain.repository

import com.example.epsviewer.domain.model.BillingState
import com.example.epsviewer.domain.model.PremiumTier
import kotlinx.coroutines.flow.Flow

/**
 * Abstract repository for billing and purchase management
 */
interface BillingRepository {
    /**
     * Flow of current billing state
     */
    val billingStateFlow: Flow<BillingState>

    /**
     * Initialize billing client
     */
    suspend fun initialize(): Boolean

    /**
     * Check if user has premium subscription
     */
    suspend fun isPremium(): Boolean

    /**
     * Check current premium tier
     */
    suspend fun getPremiumTier(): PremiumTier

    /**
     * Launch purchase flow for product
     * @param productId The product ID to purchase (from Play Console)
     * @return True if purchase was successful
     */
    suspend fun launchPurchaseFlow(productId: String): Boolean

    /**
     * Check if rewarded ad for one-time high-res export was used
     */
    suspend fun hasRewardedAdUsed(): Boolean

    /**
     * Mark rewarded ad as used
     */
    suspend fun markRewardedAdUsed(): Boolean

    /**
     * Release resources
     */
    suspend fun cleanup()
}

