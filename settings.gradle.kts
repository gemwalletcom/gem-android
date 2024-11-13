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
include(":wallet-core")
include(":wallet-core-proto")
include(":ui")
include(":ui-models")
include(":data")
include(":data:services")
include(":data:services:store")
include(":data:repositories")
include(":data:services:remote-gem")
include(":features")
include(":features:recipient")
include(":features:recipient:viewmodels")
include(":features:recipient:presents")
include(":localize")
