plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.corvels"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.corvels"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("com.github.bumptech.glide:compose:1.0.0-alpha.1")
    implementation("com.github.skydoves:landscapist-glide:2.2.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:1.4.1")
    implementation("io.github.jan-tennert.supabase:storage-kt:1.4.1")
    implementation("io.github.jan-tennert.supabase:realtime-kt:1.4.1")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:1.4.1")
    implementation("io.ktor:ktor-client-okhttp:2.3.7")

    // Serialización (para trabajar con JSON)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}