plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.gemwallet.android.blockchain"
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
}

dependencies {
    api(project(":gemcore"))
    // version catalog might not work
    //noinspection UseTomlInstead
    api("net.java.dev.jna:jna:5.15.0@aar")
    api("com.gemwallet.gemstone:gemstone:1.0.0@aar")
    // Local wallet core
    api(files("../libs/wallet-core-4.1.19-sources.jar"))
    implementation(project(":wallet-core"))
    implementation(project(":wallet-core-proto"))
    // Protobuf
    api(libs.protobuf.javalite)
    // Network
    api(libs.retrofit)
    api(libs.retrofit.converter.gson)
    api(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}