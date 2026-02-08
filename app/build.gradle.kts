plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.calorie_tracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.calorie_tracker"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // 1. Navigation (To move between screens)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 2. CameraX (For scanning)
    val cameraxVersion = "1.3.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // 3. ML Kit (Google's brain for reading Barcodes)
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // ML Kit - Image Labeling (To recognize "Apple", "Can", "Food")
    implementation("com.google.mlkit:image-labeling:17.0.7")

    // ML Kit - Text Recognition (To read Nutrition Tables)
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // 4. Hilt (For Dependency Injection - connecting files)
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")

    // 5. Hilt navigation compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // 6. Retrofit (For internet calls to get food info)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 7. Lottie (For the fancy animations)
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    // 8. ML Kit Image Labeling (Apple, Banana, etc.)
    implementation("com.google.mlkit:image-labeling:17.0.7")

    // 9. Material Design Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // 10. AppCompat (For Locale support)
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Text Recognition (For Nutrition Tables)
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Room Database (Local Storage)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // For Coroutines
    kapt("androidx.room:room-compiler:$roomVersion")

    // Use the Play Services version (Better, smaller app size, auto-updates)
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}