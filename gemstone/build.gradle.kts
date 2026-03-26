import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val gemstoneRoot = rootProject.projectDir.resolve("core/gemstone")
val gemstoneSrc = gemstoneRoot.resolve("android/gemstone/src")
val rustSrcDir = gemstoneRoot.resolve("src")
val cratesDir = rootProject.projectDir.resolve("core/crates")
val jniLibsDir = gemstoneSrc.resolve("main/jniLibs")
val generatedKotlinDir = gemstoneSrc.resolve("main/java")

android {
    namespace = "com.gemwallet.gemstone"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        consumerProguardFiles(gemstoneRoot.resolve("android/gemstone/consumer-rules.pro"))
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

    sourceSets {
        getByName("main") {
            java.srcDirs(generatedKotlinDir)
            jniLibs.srcDirs(jniLibsDir)
            manifest.srcFile(gemstoneSrc.resolve("main/AndroidManifest.xml"))
        }
        getByName("androidTest") {
            java.srcDirs(gemstoneSrc.resolve("androidTest/java"))
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

val bindgenKotlin = tasks.register<Exec>("bindgenKotlin") {
    description = "Generate Kotlin bindings from gemstone via uniffi"
    workingDir = gemstoneRoot
    inputs.dir(rustSrcDir)
    inputs.dir(cratesDir)
    inputs.file(gemstoneRoot.resolve("Cargo.toml"))
    outputs.dir(generatedKotlinDir.resolve("uniffi"))
    commandLine("just", "bindgen-kotlin")
}

val buildCargoNdk = tasks.register<Exec>("buildCargoNdk") {
    description = "Build gemstone native libraries using cargo-ndk"
    workingDir = gemstoneRoot
    inputs.dir(rustSrcDir)
    inputs.dir(cratesDir)
    inputs.file(gemstoneRoot.resolve("Cargo.toml"))
    outputs.dir(jniLibsDir)
    commandLine(
        "cargo", "ndk",
        "-t", "arm64-v8a",
        "-t", "armeabi-v7a",
        "-t", "x86_64",
        "-o", jniLibsDir.absolutePath,
        "build", "--lib"
    )
}

tasks.matching { it.name.matches(Regex("compile(Debug|Release)Kotlin")) }.configureEach {
    dependsOn(bindgenKotlin)
}
tasks.matching { it.name.matches(Regex("merge(Debug|Release)JniLibFolders")) }.configureEach {
    dependsOn(buildCargoNdk)
}

dependencies {
    api("net.java.dev.jna:jna:5.18.1@aar")
    implementation("androidx.core:core-ktx:1.17.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
