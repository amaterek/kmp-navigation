import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCompose.compiler)
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
        namespace = "amaterek.util.ui.navigation"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.file("consumer-rules.pro")
        }

        withHostTest {}

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

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core)

            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.runtime)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        commonTest.dependencies {
            implementation(libs.coroutines.test)
            implementation(libs.kotlin.test)
            implementation(libs.mockk)
            implementation(libs.turbine)
        }

        jvmTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
        }

        getByName("androidDeviceTest").dependencies {
            implementation(project(":navigation:jetpack"))
            implementation(project(":navigation:voyager"))
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.androidx.compose.ui.test.manifest)
            implementation(libs.androidx.test.junit)
            implementation(libs.androidx.test.runner)
            implementation(libs.junit)
            implementation(libs.mockk)
        }
    }
}

publishing {
    repositories {
        maven {
            publications {
                register("release", MavenPublication::class) {
                    groupId = "com.github.amaterek"
                    artifactId = "kmp-navigation"
                    version = libs.versions.libraryVersion.get()
                }
            }
        }
    }
}
