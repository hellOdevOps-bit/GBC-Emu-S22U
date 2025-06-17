pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.8.0" apply false
        id("org.jetbrains.kotlin.android") version "1.9.0" apply false
        id("de.mannodermaus.android-junit5") version "1.9.3.0" apply false
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GBCEmuS22U"
include(":app")
