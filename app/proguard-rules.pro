# EPS Viewer ProGuard Rules

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.android.qualifiers.** class * { *; }
-keep @javax.inject.** class * { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# Android Lifecycle
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }

# Google Play Billing
-keep class com.android.billingclient.** { *; }
-keepclassmembers class com.android.billingclient.** { *; }

# Google Mobile Ads
-keep class com.google.android.gms.ads.** { *; }
-keepclassmembers class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.internal.ads.** { *; }
-keepclassmembers class com.google.android.gms.internal.ads.** { *; }

# PhotoView
-keep class com.github.chrisbanes.photoview.** { *; }
-keepclassmembers class com.github.chrisbanes.photoview.** { *; }

# Timber
-keep class timber.log.Timber { *; }
-keepclassmembers class timber.log.Timber { *; }

# Model classes
-keep class com.example.epsviewer.domain.model.** { *; }
-keep class com.example.epsviewer.data.** { *; }

# Repository interfaces
-keep interface com.example.epsviewer.domain.repository.** { *; }
-keepclassmembers interface com.example.epsviewer.domain.repository.** { *; }

# ViewModels
-keep class com.example.epsviewer.ui.**.** extends androidx.lifecycle.ViewModel { *; }

# Activities and Fragments
-keep class com.example.epsviewer.ui.**.** extends androidx.appcompat.app.AppCompatActivity { *; }
-keep class com.example.epsviewer.ui.**.** extends androidx.fragment.app.Fragment { *; }

# Native methods for EPS rendering (if using JNI)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Suppress warnings
-dontwarn android.**
-dontwarn androidx.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.android.material.**
-dontwarn kotlin.**
-dontwarn kotlinx.**

