buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.gradle)
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.kotlin.serialization)
    }
}

plugins {
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.compose.compiler) apply false
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
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://maven.pkg.github.com/trustwallet/wallet-core")
            credentials {
                username = properties["gpr.user"] as? String ?: System.getenv("GRP_USERNAME")
                password = properties["gpr.key"] as? String ?: System.getenv("GRP_TOKEN")
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/gemwalletcom/core")
            credentials {
                username = properties["gpr.user"] as? String ?: System.getenv("GRP_USERNAME")
                password = properties["gpr.key"] as? String ?: System.getenv("GRP_TOKEN")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
