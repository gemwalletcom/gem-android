plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    kotlin("kapt")
}

android {
    namespace = "com.gemwallet.android.data.coordinates"
    compileSdk {
        version = release(36)
    }

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":data:repositories"))
    implementation(project(":gemcore"))
    implementation(project(":data:services:store"))
    api(project(":data:services:remote-gem"))

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.ktx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}