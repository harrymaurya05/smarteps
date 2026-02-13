plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android.plugin)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.epsviewer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.epsviewer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Vector drawable support
        vectorDrawables.useSupportLibrary = true

        // NDK configuration for Ghostscript (disabled until libgs.so is added)
        // Uncomment when you add libgs.so to app/src/main/jniLibs/
        /*
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64"))
        }

        // CMake configuration
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17", "-Wall", "-Wextra")
                arguments("-DANDROID_STL=c++_shared")
            }
        }
        */
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = false
        buildConfig = true
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    // CMake configuration for native code (Ghostscript JNI)
    // Disabled until libgs.so is added to app/src/main/jniLibs/
    // Uncomment when you have the Ghostscript native library
    /*
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    */

    packaging {
        resources {
            excludes.add("META-INF/proguard/androidx-*.pro")
            excludes.add("META-INF/androidx.*.version")
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.preferences)
    implementation(libs.androidx.work.ktx)

    // Lifecycle & Architecture
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Kotlin & Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Hilt DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Timber for logging
    implementation(libs.timber)

    // Gson for JSON
    implementation(libs.gson)

    // Image viewing
    implementation(libs.photoview)

    // PDF/EPS Enhanced Rendering (exclude old support libs to avoid AndroidX conflicts)
    implementation(libs.pdfium.android) {
        exclude(group = "com.android.support")
    }

    // Google Play Services
    implementation(libs.google.play.billing)
    implementation(libs.google.mobile.ads)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
}