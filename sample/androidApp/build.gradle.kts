plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinCompose.compiler)
}

android {
    namespace = "amaterek.util.ui.navigation.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "amaterek.util.ui.navigation.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug") // temporary solution
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
}

dependencies {
    implementation(projects.sample)
    implementation(libs.androidx.activity.compose)
    implementation(libs.amaterek.log)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
