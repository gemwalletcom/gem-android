import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    kotlin("android")
    kotlin("kapt")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    id("androidx.room")
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
}

repositories {
    google()
    mavenCentral()
}

android {
    namespace = "com.gemwallet.android"
    compileSdk = 36
    ndkVersion = "28.1.13356709"

    val channelDimension by extra("channel")
    flavorDimensions.add(channelDimension)

    defaultConfig {
        applicationId = "com.gemwallet.android"
        minSdk = 28
        targetSdk = 36
        versionCode = Integer.valueOf(System.getenv("BUILD_NUMBER") ?: "1")
        versionName = System.getenv("BUILD_VERSION") ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        splits {
            abi {
                isEnable = false
                include("arm64-v8a", "armeabi-v7a")
                isUniversalApk = false
            }
        }
    }
    productFlavors {
        create("google") {
            dimension = channelDimension
            isDefault = true
            ndk {
                abiFilters.add("armeabi-v7a")
                abiFilters.add("arm64-v8a")
            }
            buildConfigField("String", "UPDATE_URL", "\"https://play.google.com/store/apps/details?id=com.gemwallet.android\"")
        }

        create("fdroid") {
            dimension = channelDimension
            buildConfigField("String", "UPDATE_URL", "\"\"")
        }
        create("huawei") {
            dimension = channelDimension
            ndk {
                abiFilters.add("armeabi-v7a")
                abiFilters.add("arm64-v8a")
            }
            buildConfigField("String", "UPDATE_URL", "\"https://appgallery.huawei.com/app/C109713129\"")
        }
        create("solana") {
            dimension = channelDimension
            ndk {
                abiFilters.add("arm64-v8a")
            }
            buildConfigField("String", "UPDATE_URL", "\"solanadappstore://details?id=com.gemwallet.android\"")
        }
        create("universal") {
            dimension = channelDimension
            ndk {
                abiFilters.add("armeabi-v7a")
                abiFilters.add("arm64-v8a")
            }
            buildConfigField("String", "UPDATE_URL", "\"https://apk.gemwallet.com/gem_wallet_latest.apk\"")
        }
        create("samsung") {
            dimension = channelDimension
            ndk {
                abiFilters.add("armeabi-v7a")
                abiFilters.add("arm64-v8a")
            }
            buildConfigField("String", "UPDATE_URL", "\"https://apps.samsung.com/appquery/appDetail.as?appId=com.gemwallet.android\"")
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = System.getenv("ANDROID_KEYSTORE_ALIAS")
            keyPassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            storeFile = file(System.getenv("ANDROID_KEYSTORE_FILENAME") ?: "release.keystore")
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            enableV1Signing = true
            enableV2Signing  = true
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
                    abiFilters.add("x86_64")
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

    packaging {
        resources {
            excludes += "META-INF/*"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/LICENSE.md"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
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

    room {
        schemaDirectory("$projectDir/schemas")
    }

    lint {
        disable += "Instantiatable"
        checkGeneratedSources = true
        checkDependencies = true
    }
}

dependencies {
    implementation(project(":blockchain"))
    implementation(project(":ui"))
    implementation(project(":data:repositories"))

    // Features
    implementation(project(":features:activities:presents"))
    implementation(project(":features:activities:viewmodels"))
    implementation(project(":features:add_asset:presents"))
    implementation(project(":features:add_asset:viewmodels"))
    implementation(project(":features:asset:presents"))
    implementation(project(":features:asset:viewmodels"))
    implementation(project(":features:asset_select:presents"))
    implementation(project(":features:asset_select:viewmodels"))
    implementation(project(":features:banner:presents"))
    implementation(project(":features:banner:viewmodels"))
    implementation(project(":features:transfer_amount:presents"))
    implementation(project(":features:transfer_amount:viewmodels"))
    implementation(project(":features:swap:presents"))
    implementation(project(":features:swap:viewmodels"))
    implementation(project(":features:receive:presents"))
    implementation(project(":features:receive:viewmodels"))
    implementation(project(":features:wallets:presents"))
    implementation(project(":features:wallets:viewmodels"))
    implementation(project(":features:earn:stake:presents"))
    implementation(project(":features:earn:stake:viewmodels"))
    implementation(project(":features:earn:delegation:presents"))
    implementation(project(":features:earn:delegation:viewmodels"))
    implementation(project(":features:settings:aboutus:presents"))
    implementation(project(":features:settings:aboutus:viewmodels"))
    implementation(project(":features:settings:currency:presents"))
    implementation(project(":features:settings:currency:viewmodels"))
    implementation(project(":features:settings:develop:presents"))
    implementation(project(":features:settings:develop:viewmodels"))
    implementation(project(":features:settings:networks:presents"))
    implementation(project(":features:settings:networks:viewmodels"))
    implementation(project(":features:settings:price_alerts:presents"))
    implementation(project(":features:settings:price_alerts:viewmodels"))
    implementation(project(":features:settings:security:presents"))
    implementation(project(":features:settings:security:viewmodels"))
    implementation(project(":features:settings:settings:presents"))
    implementation(project(":features:settings:settings:viewmodels"))
    implementation(project(":features:recipient:presents"))
    implementation(project(":features:nft:presents"))
    implementation(project(":features:update_app:presents"))
    implementation(project(":features:wallet-details:presents"))

    implementation(libs.ktx.core)

    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.savedstate)

    implementation(libs.compose.navigation)
    implementation(libs.kotlinx.serialization.json)

    // EncryptedPreferences
    implementation(libs.androidx.security.crypto)
    // Auth
    implementation(libs.androidx.biometric)
    // Chart
    implementation(libs.vico.m3)

    implementation(libs.reorderable)

//    implementation ("io.github.ehsannarmani:compose-charts:0.1.7")

    // Google Play
    "googleImplementation"(project(":flavors:fcm"))
    "googleImplementation"(project(":flavors:google-review"))
    // Solana Store
    "solanaImplementation"(project(":flavors:fcm"))
    "solanaImplementation"(project(":flavors:review-stub"))
    // Universal
    "universalImplementation"(project(":flavors:fcm"))
    "universalImplementation"(project(":flavors:google-review"))
    // Samsung
    "samsungImplementation"(project(":flavors:fcm"))
    "samsungImplementation"(project(":flavors:review-stub"))
    // huawei
    "huaweiImplementation"(project(":flavors:pushes-stub"))
    "huaweiImplementation"(project(":flavors:review-stub"))
    // fdroid
    "fdroidImplementation"(project(":flavors:pushes-stub"))
    "fdroidImplementation"(project(":flavors:review-stub"))

    // Preview
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.junit.runner)
    testImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.uiautomator)
}