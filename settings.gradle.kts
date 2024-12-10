pluginManagement {
    repositories {
        google ()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Bevorzuge Repositories aus settings.gradle.kts
    repositories {
        google() // Google Maven Repository
        mavenCentral() // Maven Central Repository
        maven { url = uri("https://jitpack.io") } // JitPack (falls erforderlich)
    }
}

rootProject.name = "MobSysPr2"
include(":app")
 