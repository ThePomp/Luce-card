pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RobomowLuce"

// Il protocollo e' Kotlin puro: si compila e si testa anche senza SDK Android.
include(":protocol")

// L'app richiede l'SDK Android (ANDROID_HOME oppure local.properties con sdk.dir).
if (System.getenv("ANDROID_HOME") != null ||
    System.getenv("ANDROID_SDK_ROOT") != null ||
    file("local.properties").exists()
) {
    include(":app")
}
