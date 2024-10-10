plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    kotlin("android")
    kotlin("kapt")
    id("kotlinx-serialization")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("androidx.room")
    alias(libs.plugins.compose.compiler)
}

repositories {
    google()
    mavenCentral()
}

android {
    namespace = "com.gemwallet.android"
    compileSdk = 34
    ndkVersion = "26.1.10909125"

    val channelDimension by extra("channel")
    flavorDimensions.add(channelDimension)

    productFlavors {
        create("google") {
            dimension = channelDimension
            isDefault = true
        }

        create("fdroid") {
            dimension = channelDimension
        }
    }

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

            buildConfigField("String", "TEST_PHRASE", "${System.getenv("TEST_PHRASE")}")
        }

        getByName("release") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            if (System.getenv("SKIP_SIGN") == "true") {
                signingConfig = null
            } else {
                signingConfig = signingConfigs.getByName("release")
            }
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
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    kapt {
        correctErrorTypes = true
    }

    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    api(project(":blockchain"))
    // version catalog might not work
    //noinspection UseTomlInstead
    api("net.java.dev.jna:jna:5.15.0@aar")
    //noinspection UseTomlInstead
    api("com.gemwallet.gemstone:gemstone:1.0.0@aar")
    // Local wallet core
    api(files("../libs/wallet-core-4.1.5-sources.jar"))
    implementation(project(":wallet-core"))
    implementation(project(":wallet-core-proto"))
    // Protobuf
    api(libs.protobuf.javalite)

    implementation(libs.ktx.core)

    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.material3.adaptive.android)
    kapt(libs.hilt.compiler)

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.savedstate)

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
    // Auth
    implementation(libs.androidx.biometric)
    // Wallet Connect
    implementation(platform(libs.walletconnect.bom))
    implementation(libs.walletconnect.core) {
        exclude(group = "com.jakewharton.timber", module = "timber")
    }
    implementation(libs.walletconnect.web3wallet)
    // Chart
    implementation(libs.vico.m3)

    implementation(libs.reorderable)

    // Google Play
    // Notifications - FCM
    "googleImplementation"(libs.firebase.messaging)
    // In App review
    "googleImplementation"(libs.play.review)
    "googleImplementation"(libs.play.review.ktx)

    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.junit.runner)
    testImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.uiautomator)
}

room {
    schemaDirectory("$projectDir/schemas")
}