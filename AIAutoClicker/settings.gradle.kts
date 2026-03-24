pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://chaquo.com/maven")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://chaquo.com/maven")
        maven("https://jitpack.io")
    }
}

rootProject.name = "AIAutoClicker"
include(":app")
