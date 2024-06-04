plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    kotlin("android")
    kotlin("kapt")
    id("kotlinx-serialization")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("org.mozilla.rust-android-gradle.rust-android") version "0.9.3"
}

repositories {
    google()
    mavenCentral()
}

cargo {
    prebuiltToolchains = true
    targetDirectory = "$rootDir/core/target" // workspace target folder
    module = "$rootDir/core/gemstone" // Cargo.toml folder
    libname = "gemstone"
    pythonCommand = "python3"
    // profile = "release"
    targets = listOf("arm64", "arm", "x86_64")
    extraCargoBuildArguments = listOf("--lib")
    verbose = false
}

android {
    namespace = "com.gemwallet.android"
    compileSdk = 34
    ndkVersion = "26.1.10909125"

    if (System.getenv("CI") == "true") {
        testBuildType = "ci"
    } else {
        testBuildType = "debug"
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
        }

        create("ci") {
            initWith(getByName("debug"))
            ndk {
                abiFilters.add("x86_64")
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
        kotlinCompilerExtensionVersion = "1.5.9"
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
    implementation("androidx.core:core-ktx:1.13.1")

    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    implementation("androidx.activity:activity-compose:1.9.0")

    implementation("androidx.compose.ui:ui:${Config.Versions.compose}")
    implementation("androidx.compose.foundation:foundation:${Config.Versions.compose}")
    implementation("androidx.compose.material:material:${Config.Versions.compose}")
    implementation("androidx.compose.material:material-icons-core:${Config.Versions.compose}")
    implementation("androidx.compose.material:material-icons-extended:${Config.Versions.compose}")
    implementation("androidx.compose.material3:material3:${Config.Versions.material3}")
    implementation("androidx.compose.material3:material3-window-size-class:${Config.Versions.material3}")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")

    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Permissions request
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // QRCode scanner: only for none private data: recipient, memo, amount, etc
    implementation("androidx.camera:camera-camera2:${Config.Versions.camerax}")
    implementation("androidx.camera:camera-lifecycle:${Config.Versions.camerax}")
    implementation("androidx.camera:camera-view:${Config.Versions.camerax}")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Room - ORM
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")

    // QR Code
    implementation("com.google.zxing:core:3.5.3")
    // EncryptedPreferences
    implementation("androidx.security:security-crypto:1.0.0")
    // Notifications - FCM
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    // Auth
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    // Wallet Connect
    implementation(platform("com.walletconnect:android-bom:1.31.3"))
    implementation("com.walletconnect:android-core")
    implementation("com.walletconnect:web3wallet")
    // Chart
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.19")
    // In App review
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")

    debugImplementation("androidx.compose.ui:ui-tooling:${Config.Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Config.Versions.compose}")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7")
}

afterEvaluate {
    android.applicationVariants.all { variant ->
        var productFlavor = ""
        variant.productFlavors.forEach {
            productFlavor += it.name.replaceFirstChar(Char::titlecase)
        }
        val buildType = variant.buildType.name.replaceFirstChar(Char::titlecase)
        val taskName = "generate" + productFlavor + buildType + "Assets"
        logger.warn("make $taskName depend on cargoBuild")
        val generateTasks = getTasksByName(taskName, false)
        val cargoTasks = getTasksByName("cargoBuild", false)
        generateTasks.forEach {
            it.dependsOn(cargoTasks)
        }
        return@afterEvaluate
    }
}
