plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    kotlin("kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.gemwallet.android.service.store"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation(project(":gemcore"))

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.room.runtime)

    implementation(libs.ktx.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}