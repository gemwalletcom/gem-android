pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = "wallet"
include (":app")
include(":blockchain")
include(":gemcore")
