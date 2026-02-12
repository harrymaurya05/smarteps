package com.example.epsviewer.di

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.epsviewer.data.repository.EpsRepositoryImpl
import com.example.epsviewer.data.repository.BillingRepositoryImpl
import com.example.epsviewer.domain.repository.EpsRepository
import com.example.epsviewer.domain.repository.BillingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideEpsRepository(
        @ApplicationContext context: Context
    ): EpsRepository = EpsRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideBillingRepository(
        @ApplicationContext context: Context
    ): BillingRepository = BillingRepositoryImpl(context)

    @Provides
    @Singleton
    fun providePreferenceManager(
        @ApplicationContext context: Context
    ) = PreferenceManager.getDefaultSharedPreferences(context)
}

