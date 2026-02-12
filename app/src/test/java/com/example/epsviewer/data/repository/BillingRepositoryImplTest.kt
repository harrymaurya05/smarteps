package com.example.epsviewer.data.repository

import com.example.epsviewer.domain.model.PremiumTier
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class BillingRepositoryImplTest {

    private val repository = BillingRepositoryImpl(
        android.app.Application()
    )

    @Test
    fun `initialize sets ready state`() = runBlocking {
        val result = repository.initialize()
        assertTrue(result)
    }

    @Test
    fun `free user is not premium`() = runBlocking {
        repository.initialize()
        val isPremium = repository.isPremium()
        assertFalse(isPremium)
    }

    @Test
    fun `can simulate monthly purchase`() = runBlocking {
        repository.initialize()
        val success = repository.launchPurchaseFlow("com.example.epsviewer.monthly")
        assertTrue(success)

        val tier = repository.getPremiumTier()
        assertEquals(PremiumTier.MONTHLY_SUBSCRIBER, tier)
    }

    @Test
    fun `can simulate annual purchase`() = runBlocking {
        repository.initialize()
        val success = repository.launchPurchaseFlow("com.example.epsviewer.annual")
        assertTrue(success)

        val tier = repository.getPremiumTier()
        assertEquals(PremiumTier.ANNUAL_SUBSCRIBER, tier)
    }

    @Test
    fun `can simulate lifetime purchase`() = runBlocking {
        repository.initialize()
        val success = repository.launchPurchaseFlow("com.example.epsviewer.lifetime")
        assertTrue(success)

        val tier = repository.getPremiumTier()
        assertEquals(PremiumTier.LIFETIME_SUBSCRIBER, tier)
    }

    @Test
    fun `rewarded ad tracking works`() = runBlocking {
        repository.initialize()
        assertFalse(repository.hasRewardedAdUsed())

        val marked = repository.markRewardedAdUsed()
        assertTrue(marked)

        // Note: actual persistence would need SharedPreferences mock
        // This test validates the API contract
    }
}

