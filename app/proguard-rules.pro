# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to hide the original source file name.
#-renamesourcefileattribute SourceFile


#####################################################
### Gson ProGuard and R8 rules
#####################################################

# Keep generic signatures (needed for type resolution with Gson)
-keepattributes Signature

# Keep Gson annotations
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Keep class TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep any classes extending TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken

# Keep classes with @JsonAdapter annotation
-keep,allowobfuscation,allowoptimization @com.google.gson.annotations.JsonAdapter class *

# Keep fields with @SerializedName annotation
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep fields with other Gson annotations
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.Expose <fields>;
  @com.google.gson.annotations.JsonAdapter <fields>;
  @com.google.gson.annotations.Since <fields>;
  @com.google.gson.annotations.Until <fields>;
}

# Keep no-args constructors for Gson adapters
-keepclassmembers class * extends com.google.gson.TypeAdapter {
  <init>();
}
-keepclassmembers class * implements com.google.gson.TypeAdapterFactory {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonSerializer {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonDeserializer {
  <init>();
}

# If a class has @SerializedName fields and a no-args constructor, keep them
-if class *
-keepclasseswithmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
  @com.google.gson.annotations.SerializedName <fields>;
}

#####################################################
### Jackson (if used)
#####################################################
-keepnames class com.fasterxml.jackson.databind.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

#####################################################
### Your domain models (example package)
#####################################################
-keepclassmembers class  com.example.core.domain.azkar.DomainZekr { !transient <fields>; }
-keepclassmembers class  com.example.core.domain.DomainNameOfAllah { !transient <fields>; }
-keepclassmembers class  com.elsharif.dailyseventy.domain.azan.prayermethods.PrayerTimesMethodsResponse { !transient <fields>; }
-keepclassmembers class com.elsharif.dailyseventy.domain.azan.prayermethods.Data { !transient <fields>; }

# Keep all pojo classes (safe fallback)
-keep class com.elsharif.dailyseventy.domain.azan.** { *; }

#####################################################
### General Android stuff (optional but common)
#####################################################

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}



# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
  static ** CREATOR;
}

# Keep Activities (needed for manifest)
-keep class * extends android.app.Activity

# Keep Fragments (needed for FragmentManager)
-keep class * extends androidx.fragment.app.Fragment
