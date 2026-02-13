plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.5.31"
    id("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" // ✅ Add version here
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")



}

android {
    namespace = "com.elsharif.dailyseventy"
    compileSdk = 35

    lint {
        abortOnError = false
        warningsAsErrors = false
        disable += listOf(
            "NullSafeMutableLiveData"
        )
    }
    defaultConfig {
        applicationId = "com.elsharif.dailyseventy"
        minSdk = 24
        targetSdk = 35

        versionCode = 6
        versionName = "1.5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental"     to "true",
                    "room.expandProjection" to  "true"
                )
            }
        }
    }



    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }

    signingConfigs {
        create("DailySeventy") {
            storeFile = file("E:\\AndroidStudioProjects\\DailySeventy\\keystore\\my_release_key.jks")        // مكان ملف keystore
            storePassword = project.property("STORE_PASSWORD") as String
            keyAlias = project.property("KEY_ALIAS") as String
            keyPassword = project.property("KEY_PASSWORD") as String
        }
    }

    buildTypes {
        release {

            isCrunchPngs = true
            isMinifyEnabled = true   // يفضل تعمل shrink/obfuscation
            isShrinkResources = true // يقلل حجم الـ APK/Bundle
            signingConfig = signingConfigs.getByName("DailySeventy") // نعرّف تحت

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    compileOptions {
        isCoreLibraryDesugaringEnabled  =false

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf("-Xcontext-receivers")

    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    composeOptions {
//        kotlinCompilerExtensionVersion '1.6.10'  // Matches Compose 1.6.x
        //   kotlinCompilerVersion '2.1.0'            // Updated Kotlin compiler version
        kotlinCompilerExtensionVersion = "1.5.13"

    }
}

dependencies {


    // Core module
    implementation(project(":core"))

    // Core libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")


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
    implementation ("androidx.glance:glance-material3:1.1.1")
    implementation ("androidx.glance:glance-appwidget-preview:1.1.1")
    implementation ("androidx.glance:glance-preview:1.1.1")



    // retrofit

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")

    implementation ("com.google.code.gson:gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    /*

    // Dimension Support
        implementation("com.intuit.sdp:sdp-android:1.0.6")

    // Location Services
        implementation("com.google.android.gms:play-services-location:21.0.1")

    // Localization
        implementation("com.akexorcist:localization:1.2.11")

    // Hijri Calendar View
        implementation("com.github.eltohamy:material-hijri-calendarview:1.1.2")

    // Umm al-Qura Calendar
        implementation("com.github.msarhan:ummalqura-calendar:1.1.9")

    // Time4J for Android
        implementation("net.time4j:time4j-android:4.8-2021a")

    */

    implementation ("com.github.msarhan:ummalqura-calendar:1.1.9")


    implementation("net.time4j:time4j-android:4.8-2021a")


    // WorkManager
    implementation ("androidx.work:work-runtime:2.10.1")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


    //Hilt Worker support
    implementation ("androidx.hilt:hilt-work:1.2.0")


    //Sheets Compose
    implementation ("com.maxkeppeler.sheets-compose-dialogs:core:1.2.0")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:list:1.2.0")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:calendar:1.2.0")


    implementation("androidx.lifecycle:lifecycle-process:2.9.3")
    // دا اللي فيه ViewTreeLifecycleOwner
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
// علشان ViewTreeSavedStateRegistryOwner
    implementation("androidx.savedstate:savedstate:1.3.2")

    implementation("androidx.core:core-splashscreen:1.0.1")


}