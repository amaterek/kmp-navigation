plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

kotlin {
    jvm()
    jvmToolchain(libs.versions.jvmTarget.get().toInt())

    if (findProperty("build.ios") == "true") {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach {
            it.binaries.framework {
                baseName = "util.ui.navigation"
                isStatic = true
            }
        }
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.jvmTarget.get()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.navigation)
        }

        androidMain.dependencies {
            implementation(compose.runtime)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlin.reflect)
        }
    }

    android {
        publishLibraryVariants("release")
    }

    task("testClasses")
}

android {
    namespace = "amaterek.util.ui.navigation"

    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        consumerProguardFile("consumer-rules.pro")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

publishing {
    repositories {
        maven {
            publications {
                register("release", MavenPublication::class) {
                    groupId = "com.github.amaterek"
                    artifactId = "kmp-navigation-jetpack"
                    version = libs.versions.libraryVersion.get()
                }
            }
        }
    }
}