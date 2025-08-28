plugins {
    alias(libs.plugins.android.library)
    kotlin("android")
}

android {
    namespace = "com.gemwallet.features.settings"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}