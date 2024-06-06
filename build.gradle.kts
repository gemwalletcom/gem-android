buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.gradle)
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.kotlin.serialization)
        classpath(libs.rust.plugin)
    }
}

plugins {
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.google.services) apply false
}

allprojects {
    repositories {
        val propFile = File(rootDir.absolutePath, "local.properties")
        var properties = java.util.Properties()
        if (propFile.exists()) {
            properties = properties.apply {
                propFile.inputStream().use { fis ->
                    load(fis)
                }
            }
        }
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://maven.pkg.github.com/trustwallet/wallet-core")
            credentials {
                username = properties["gpr.user"] as? String ?: System.getenv("GRP_USERNAME")
                password = properties["gpr.key"] as? String ?: System.getenv("GRP_TOKEN")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}