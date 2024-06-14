plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    kotlin("android")
    kotlin("kapt")
    id("kotlinx-serialization")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.github.willir.rust.cargo-ndk-android")
}

repositories {
    google()
    mavenCentral()
}

cargoNdk {
    targets = if (System.getenv("UNIT_TESTS") == "true") {
        arrayListOf("x86_64")
    } else {
        arrayListOf("x86_64", "armeabi-v7a", "arm64-v8a")
    }
    module = "core/gemstone"
    targetDirectory = "/../target"
    librariesNames = arrayListOf("libgemstone.so")
    extraCargoBuildArguments = arrayListOf("--lib")
}

android {
    namespace = "com.gemwallet.android"
    compileSdk = 34
    ndkVersion = "26.1.10909125"

    defaultConfig {
        applicationId = "com.gemwallet.android"
        minSdk = 28
        targetSdk = 34
        versionCode = Integer.valueOf(System.getenv("BUILD_NUMBER") ?: "1")
        versionName = System.getenv("BUILD_VERSION") ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            abiFilters.add("x86_64")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }

        splits {
            abi {
                isEnable = false
                include("arm64-v8a", "armeabi-v7a")
                isUniversalApk = false
            }
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = System.getenv("ANDROID_KEYSTORE_ALIAS")
            keyPassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            storeFile = file(System.getenv("ANDROID_KEYSTORE_FILENAME") ?: "release.keystore")
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true

            if (System.getenv("UNIT_TESTS") == "true") {
                ndk {
                    abiFilters.remove("arm64-v8a")
                    abiFilters.remove("armeabi-v7a")
                }

                splits {
                    abi {
                        reset()
                        isEnable = false
                        include("x86_64")
                        isUniversalApk = false
                    }
                }
            }
        }

        getByName("release") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    api(project(":blockchain"))
    implementation(libs.ktx.core)

    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.compose.activity)

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.window.size)
    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.coil.compose)

    implementation(libs.compose.navigation)
    implementation(libs.kotlinx.serialization.json)

    // Permissions request
    implementation(libs.compose.permissions)

    // QRCode scanner: only for none private data: recipient, memo, amount, etc
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.barcode.scanning)

    // Room - ORM
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.room.runtime)

    // QR Code
    implementation(libs.zxing.core)
    // EncryptedPreferences
    implementation(libs.androidx.security.crypto)
    // Notifications - FCM
    implementation(libs.firebase.messaging)
    // Auth
    implementation(libs.androidx.biometric)
    // Wallet Connect
    implementation(platform(libs.walletconnect.bom))
    implementation(libs.walletconnect.core)
    implementation(libs.walletconnect.web3wallet)
    // Chart
    implementation(libs.vico.m3)
    // In App review
    implementation(libs.play.review)
    implementation(libs.play.review.ktx)

    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
