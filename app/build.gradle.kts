plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.5.31"
    id("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
   // id("com.google.devtools.ksp")


}

android {
    namespace = "com.elsharif.dailyseventy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.elsharif.dailyseventy"
        minSdk = 24
        targetSdk = 34
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



    // Gson ,serialization
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")



    // Room
    val roomVersion = "2.6.0"
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Navigation
    implementation ("androidx.navigation:navigation-compose:2.9.0")

    // Extended Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.2")

    implementation ("io.coil-kt:coil-compose:1.4.0")


    // OpenStreetMap (OSM)
    implementation ("org.osmdroid:osmdroid-android:6.1.16")


    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")

    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")


//    implementation ("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    kapt ("androidx.hilt:hilt-compiler:1.2.0")

    implementation ("androidx.appcompat:appcompat:1.7.0")


    // Navigation
    implementation ("androidx.navigation:navigation-compose:2.9.0")

    //fonts
    implementation("androidx.compose.ui:ui-text-google-fonts:1.8.1")

    //Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

/*
    //compose destination
    val destinationVersion = "1.9.52"
    implementation("io.github.raamcosta.compose-destinations:core:$destinationVersion")
    ksp("io.github.raamcosta.compose-destinations:ksp:$destinationVersion")
*/


    // SnackbarEvent
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))


    //Curved-Scroll
    //noinspection UseTomlInstead
    implementation ("com.github.mohamedtamer0:Compose-Curved-Scroll-library:1.0")





    //For Work Manager
    implementation (libs.play.services.location)


    //For Widgets

    implementation ("androidx.glance:glance:1.1.1")
    implementation ("androidx.glance:glance-appwidget:1.1.1")




}