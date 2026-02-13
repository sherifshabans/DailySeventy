# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

#####################################################
### Gson ProGuard and R8 rules
#####################################################

-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keep class com.google.gson.reflect.TypeToken { *; }
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowoptimization @com.google.gson.annotations.JsonAdapter class *

# Keep the custom Application class (IMPORTANT)
-keep class com.elsharif.dailyseventy.DilayApp { *; }

# Keep Gson-related fields
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.Expose <fields>;
  @com.google.gson.annotations.JsonAdapter <fields>;
  @com.google.gson.annotations.Since <fields>;
  @com.google.gson.annotations.Until <fields>;
}

-keepclassmembers class * extends com.google.gson.TypeAdapter { <init>(); }
-keepclassmembers class * implements com.google.gson.TypeAdapterFactory { <init>(); }
-keepclassmembers class * implements com.google.gson.JsonSerializer { <init>(); }
-keepclassmembers class * implements com.google.gson.JsonDeserializer { <init>(); }

-if class *
-keepclasseswithmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
  @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

#####################################################
### Jackson (if used)
#####################################################
-keepnames class com.fasterxml.jackson.databind.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

#####################################################
### Your domain models
#####################################################
-keepclassmembers class  com.example.core.domain.azkar.DomainZekr { !transient <fields>; }
-keepclassmembers class  com.example.core.domain.DomainNameOfAllah { !transient <fields>; }
-keepclassmembers class  com.elsharif.dailyseventy.domain.azan.prayermethods.PrayerTimesMethodsResponse { !transient <fields>; }
-keepclassmembers class com.elsharif.dailyseventy.domain.azan.prayermethods.Data { !transient <fields>; }

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep,allowobfuscation @interface com.google.gson.annotations.SerializedName

# Keep all pojo classes
-keep class com.elsharif.dailyseventy.domain.azan.** { *; }

#####################################################
### AndroidX / Dagger / Hilt / CoreComponent
#####################################################

# Keep androidx core and support classes
-keep class androidx.core.** { *; }
-keep interface androidx.core.** { *; }

# Keep Hilt/Dagger generated classes
-keep class dagger.hilt.** { *; }
-keep class com.elsharif.dailyseventy.**_HiltComponents { *; }
-keep class com.elsharif.dailyseventy.**_Factory { *; }
-keep class com.elsharif.dailyseventy.**_MembersInjector { *; }

# Keep androidx startup (used by Hilt)
-keep class androidx.startup.** { *; }
-keep interface androidx.startup.** { *; }

#####################################################
### Firebase / WorkManager / OkHttp / Retrofit
#####################################################

-keep class com.google.firebase.** { *; }
-keep class androidx.work.** { *; }

# Retrofit & OkHttp
-keep interface retrofit2.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

#####################################################
### Kotlin Coroutines
#####################################################
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

#####################################################
### Android essentials
#####################################################

# Keep Activities (manifest)
-keep class * extends android.app.Activity

# Keep Fragments
-keep class * extends androidx.fragment.app.Fragment

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
  static ** CREATOR;
}
