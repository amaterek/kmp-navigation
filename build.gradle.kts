plugins {
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinParcelize) apply false
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