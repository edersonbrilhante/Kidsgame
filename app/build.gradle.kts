plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.kidsgames"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kidsgames"
        minSdk = 26
        targetSdk = 35
        // CI passes VERSION_CODE (the run number) so each build is an upgrade; 1 locally.
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = "1.0.${System.getenv("VERSION_CODE") ?: "0"}"
    }

    signingConfigs {
        // Stable key so every build has the same signature (required for in-place upgrades).
        // This is a debug key committed for family sideloading — NOT for the Play Store.
        getByName("debug") {
            val ks = file("matteo-debug.keystore")
            if (ks.exists()) {
                storeFile = ks
                storePassword = "matteo123"
                keyAlias = "matteo"
                keyPassword = "matteo123"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.datastore.preferences)
    debugImplementation(libs.androidx.ui.tooling)
}
