import java.net.URI

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = URI("https://jitpack.io") }
    }
}

rootProject.name = "kmp-navigation"

// TODO Temporary solution: the JitPack build of kmp-log has no iOS artifacts, so the local kmp-log is used instead
includeBuild("../kmp-log") {
    dependencySubstitution {
        substitute(module("com.github.amaterek.kmp-log:log")).using(project(":log"))
    }
}
include(":navigation")
include(":navigation:dialog")
include(":navigation:jetpack")
include(":navigation:voyager")
include(":sample")
include(":sample:androidApp")
