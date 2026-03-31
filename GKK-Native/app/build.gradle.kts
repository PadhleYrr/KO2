plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

android {
    namespace   = "com.gkk.mppsc"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.gkk.mppsc"
        minSdk          = 26          // Android 8+ — covers 95%+ of active devices
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0.0"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            // These are injected by GitHub Actions secrets
            storeFile     = file(System.getenv("KEYSTORE_PATH")     ?: "release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")      ?: ""
            keyAlias      = System.getenv("KEY_ALIAS")              ?: ""
            keyPassword   = System.getenv("KEY_PASSWORD")           ?: ""
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable        = true
        }
        release {
            isMinifyEnabled     = true
            isShrinkResources   = true
            signingConfig       = signingConfigs.getByName("release")
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // DataStore — replaces localStorage
    implementation(libs.androidx.datastore.prefs)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)

    // Data
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines)

    // System UI
    implementation(libs.accompanist.systemuicontroller)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
}
