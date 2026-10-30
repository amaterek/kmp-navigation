import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCompose.compiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidMultiplatformLibrary)
    id("maven-publish")
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }
    }

    if (findProperty("build.ios") == "true") {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach {
            it.binaries.framework {
                baseName = "util.ui.navigation"
                isStatic = true
            }
        }
    }

    android {
        namespace = "amaterek.util.ui.navigation.dialog"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.file("consumer-rules.pro")
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        packaging {
            resources {
                pickFirsts += "/META-INF/LICENSE*.md"
            }
        }
    }

    // Desktop and iOS share the skiko implementation of dialogs
    applyDefaultHierarchyTemplate {
        common {
            group("skiko") {
                withJvm()
                group("ios")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.navigation)

            implementation(libs.compose.foundation)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.jetbrains.navigationevent.compose)
        }

        commonTest.dependencies {
            implementation(projects.navigation.jetpack)
            implementation(projects.navigation.nav3)
            implementation(projects.navigation.voyager)
            implementation(libs.compose.ui.test)
            implementation(libs.kotlin.test)
        }

        jvmTest.dependencies {
            implementation(rootProject.extra["composeDesktopCurrentOs"] as String)
        }

        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.androidx.compose.ui.test.manifest)
            implementation(libs.androidx.test.junit)
            implementation(libs.androidx.test.runner)
            implementation(libs.junit)
        }
    }
}

publishing {
    repositories {
        maven {
            publications {
                register("release", MavenPublication::class) {
                    groupId = "com.github.amaterek"
                    artifactId = "kmp-navigation-dialog"
                    version = libs.versions.libraryVersion.get()
                }
            }
        }
    }
}
