plugins {
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinCompose.plugin) apply false
    alias(libs.plugins.kotlinCompose.compiler) apply false
    alias(libs.plugins.detekt)
}

detekt {
    parallel = true
    allRules = true
    config = files("$rootDir/config/detekt/config.yml")
    source = files(
        fileTree(rootDir).matching {
            include("**/src/*/java/**/*.kt")
            include("**/src/*/kotlin/**/*.kt")
            include("**/*.gradle.kts")
        }
    )
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

// The Compose Desktop artifact of the host (compose.desktop.currentOs without the Compose plugin), which provides the
// native Skiko library to the UI tests on the JVM
val composeDesktopCurrentOs by extra {
    val os = System.getProperty("os.name").lowercase().let {
        when {
            it.startsWith("mac") -> "macos"
            it.startsWith("windows") -> "windows"
            else -> "linux"
        }
    }
    val arch = if (System.getProperty("os.arch") in listOf("aarch64", "arm64")) "arm64" else "x64"
    "org.jetbrains.compose.desktop:desktop-jvm-$os-$arch:${libs.versions.compose.multiplatform.get()}"
}