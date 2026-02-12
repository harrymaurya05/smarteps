package com.example.epsviewer.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class EpsViewerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupLogging()
        setupAds()
    }

    private fun setupLogging() {
        // Always plant debug tree in development
        Timber.plant(Timber.DebugTree())
    }

    private fun setupAds() {
        // TODO: Enable when running on real device or with proper Google Play Services
        // Mobile Ads can cause crashes on emulator without proper Play Services
        /*
        try {
            // Initialize Mobile Ads SDK in background thread
            MobileAds.initialize(this) { }
            Timber.d("Mobile Ads initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Mobile Ads")
        }
        */
        Timber.d("Mobile Ads disabled for development")
    }
}

