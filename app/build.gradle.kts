plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Maps API key, resolved (in order) from local.properties, a Gradle property
// (-PMAPS_API_KEY=…), or the MAPS_API_KEY env var. Never committed; defaults to
// empty so the project still builds without it (the map renders blank).
val mapsApiKey: String = run {
    val props = java.util.Properties()
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) localFile.inputStream().use { props.load(it) }
    props.getProperty("MAPS_API_KEY")
        ?: (project.findProperty("MAPS_API_KEY") as String?)
        ?: System.getenv("MAPS_API_KEY")
        ?: ""
}

android {
    namespace = "com.tripplanner.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tripplanner.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Signed with the debug key so the release APK is installable straight from
            // the GitHub Release for testing. Replace with a real upload/release keystore
            // (wired via secrets) before publishing to the Play Store.
            signingConfig = signingConfigs.getByName("debug")
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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
