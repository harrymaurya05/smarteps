package com.example.epsviewer.domain.repository

import com.example.epsviewer.domain.model.BillingState
import com.example.epsviewer.domain.model.PremiumTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Mock implementation of BillingRepository for testing
 */
class MockBillingRepository : BillingRepository {
    private val _billingStateFlow = MutableStateFlow(
        BillingState(tier = PremiumTier.FREE, isReady = true)
    )

    override val billingStateFlow: Flow<BillingState> = _billingStateFlow

    override suspend fun initialize(): Boolean = true

    override suspend fun isPremium(): Boolean {
        return _billingStateFlow.value.tier != PremiumTier.FREE
    }

    override suspend fun getPremiumTier(): PremiumTier {
        return _billingStateFlow.value.tier
    }

    override suspend fun launchPurchaseFlow(productId: String): Boolean {
        val newTier = when {
            productId.contains("monthly") -> PremiumTier.MONTHLY_SUBSCRIBER
            productId.contains("annual") -> PremiumTier.ANNUAL_SUBSCRIBER
            productId.contains("lifetime") -> PremiumTier.LIFETIME_SUBSCRIBER
            else -> PremiumTier.FREE
        }
        _billingStateFlow.value = _billingStateFlow.value.copy(tier = newTier)
        return true
    }

    override suspend fun hasRewardedAdUsed(): Boolean = false

    override suspend fun markRewardedAdUsed(): Boolean = true

    override suspend fun cleanup() {}

    fun setTier(tier: PremiumTier) {
        _billingStateFlow.value = _billingStateFlow.value.copy(tier = tier)
    }
}

