plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.thekirankumarv.newsync"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thekirankumarv.newsync"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
    }
    
}

dependencies {

    // Lottie
    implementation(libs.lottie)
    implementation (libs.lottie.compose)

    // Compose navigation
    implementation(libs.androidx.navigation.compose)

    // Kotlin serialization
    implementation(libs.kotlinx.serialization.json)

    // Splash
    implementation(libs.androidx.core.splashscreen.v100)

    // Material3
    implementation(libs.material3)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.google.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.crashlytics)
    implementation (libs.firebase.storage.ktx)

    // live data
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Coil
    implementation(libs.coil.compose)

    // Facebook SDK
    implementation (libs.facebook.android.sdk.v1702)

    // Dagger Hilt
    implementation(libs.hilt.android)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.messaging)
    kapt(libs.hilt.android.compiler)
    implementation (libs.androidx.hilt.navigation.compose)

    // Retrofit
    implementation(libs.okhttp)
    implementation (libs.retrofit)
    implementation (libs.converter.gson.v290)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // Compose Foundation
    implementation (libs.androidx.foundation)

    // Accompanist
    implementation (libs.accompanist.systemuicontroller)

    // Paging 3
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Glide
    implementation (libs.glide)

    implementation (libs.androidx.datastore.preferences)
    implementation (libs.androidx.appcompat)

    // Biometric
    implementation(libs.androidx.biometric)

    implementation(libs.androidx.activity.ktx)
    implementation (libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.espresso.core)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}



