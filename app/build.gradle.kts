plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.panichelper"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.panichelper"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
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
        isCoreLibraryDesugaringEnabled = true
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
    // Core / Lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("com.google.android.material:material:1.12.0")


    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.compose.material.icons.extended)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // DataStore (für persistente Speicherung der Telefonnummer)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Serialisierung der gespeicherten Vertrauenspersonen-Liste als JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // java.time (LocalDate) auf minSdk 24 nutzbar machen (für die Streak-Berechnung)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")

    // Per-App-Sprachumschaltung (funktioniert auch ohne AppCompatActivity)
    implementation("androidx.appcompat:appcompat:1.7.0")

    // ExoPlayer für die Hintergrundmusik: im Gegensatz zu MediaPlayer sind
    // play()/seekTo()/etc. nicht-blockierende Aufrufe (werden intern an einen
    // eigenen Wiedergabe-Thread weitergereicht), sodass ein hängender/fehlerhafter
    // Decoder nie unseren Coroutine-Mutex und damit die ganze Musik-Wiedergabe
    // dauerhaft blockieren kann - genau das Problem, das MediaPlayer auf manchen
    // Geräten (u. a. Pixel 8 Pro) verursacht hat.
    implementation("androidx.media3:media3-exoplayer:1.5.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


}