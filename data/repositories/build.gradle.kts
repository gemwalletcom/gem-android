plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    kotlin("kapt")
}

android {
    namespace = "com.gemwallet.android.data.repositoreis"
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
    buildFeatures {
        buildConfig = true
    }
    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation(project(":gemcore"))
    implementation(project(":data:services:store"))
    api(project(":data:services:remote-gem"))

    // Wallet Connect
    api(platform(libs.walletconnect.bom))
    api(libs.walletconnect.core) {
        exclude(group = "com.jakewharton.timber", module = "timber")
    }
    api(libs.walletconnect.web3wallet)
    api(libs.datastore)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.ktx.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}