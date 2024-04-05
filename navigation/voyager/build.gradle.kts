plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.androidLibrary)
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
            api(projects.navigation)
            implementation(compose.foundation)
            implementation(compose.runtime)
            implementation(compose.ui)

            implementation(libs.voyager.core)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.transitions)
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
}

android {
    namespace = "amaterek.util.ui.navigation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        consumerProguardFile("consumer-rules.pro")
    }

    dependencies {
        testImplementation(libs.junit)
        testImplementation(libs.mockk)
    }
}

publishing {
    repositories {
        maven {
            publications {
                register("release", MavenPublication::class) {
                    groupId = "com.github.amaterek"
                    artifactId = "kmp-navigation-voyager"
                    version = libs.versions.libraryVersion.get()
                }
            }
        }
    }
}