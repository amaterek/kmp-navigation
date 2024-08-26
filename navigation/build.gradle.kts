import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinParcelize)
    id("maven-publish")
}

kotlin {
    jvm()

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
            api(libs.coroutines.core)

            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.runtime)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        commonTest.dependencies {
            implementation(libs.coroutines.test)
            implementation(libs.kotlin.test)
            implementation(libs.mockk.common)
            implementation(libs.turbine)
        }

        jvmTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
        }
    }

    android {
        publishLibraryVariants("release")
    }

    task("testClasses")

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }
}

dependencies {
    androidTestImplementation(project(":navigation:jetpack"))
    androidTestImplementation(project(":navigation:voyager"))
    androidTestImplementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.androidx.appcompat)
    androidTestImplementation(libs.androidx.lifecycle.runtime)
    androidTestImplementation(libs.compose.material3)
    androidTestImplementation(libs.compose.ui)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.mockk)
}

android {
    namespace = "amaterek.util.ui.navigation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        consumerProguardFile("consumer-rules.pro")
    }

    dependencies {
        testImplementation(libs.junit)
        testImplementation(libs.mockk)
    }

    packaging {
        resources {
            pickFirsts += "/META-INF/LICENSE*.md"
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