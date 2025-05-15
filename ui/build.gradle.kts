plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.gemwallet.android.ui"
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
        resources {
            excludes += "META-INF/*"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/LICENSE.md"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

dependencies {
    api(project(":ui-models"))

    // Compose
    api(libs.androidx.material3.adaptive.android)
    api(libs.compose.ui)
    api(libs.compose.material.icons.core)
    api(libs.compose.material.icons.extended)
    api(libs.compose.material3)
    api(libs.compose.material3.window.size)
    api(libs.kotlinx.collections.immutable)
    api(libs.compose.activity)

    // QRCode scanner: only for none private data: recipient, memo, amount, etc
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.barcode.scanning)
    // QR Code
    api(libs.zxing.core)

    // Images
    api(libs.coil.compose)
    api(libs.coil.network)
    api(libs.coil.svg)


    // Permissions request
    api(libs.compose.permissions)

    implementation(libs.ktx.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}