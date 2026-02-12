package com.example.epsviewer.domain.model

/**
 * Represents the premium tier state of the app
 */
enum class PremiumTier {
    FREE,
    MONTHLY_SUBSCRIBER,
    ANNUAL_SUBSCRIBER,
    LIFETIME_SUBSCRIBER
}

/**
 * Represents purchase information for a premium subscription
 */
data class PurchaseInfo(
    val tier: PremiumTier,
    val purchaseTime: Long,
    val purchaseToken: String,
    val orderId: String,
    val autoRenewing: Boolean = false
)

/**
 * Represents the current billing state
 */
data class BillingState(
    val tier: PremiumTier = PremiumTier.FREE,
    val purchases: Map<String, PurchaseInfo> = emptyMap(),
    val isReady: Boolean = false,
    val error: String? = null
)

