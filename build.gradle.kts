// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id ("org.jetbrains.kotlin.plugin.serialization") version "1.5.31" // Use the latest version
    id("com.google.dagger.hilt.android") version "2.50" apply false
    alias(libs.plugins.android.library) apply false
     id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
     id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"


}
buildscript {


    dependencies {
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.50")

        classpath ("com.android.tools.build:gradle:8.5.0")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")

    }
}