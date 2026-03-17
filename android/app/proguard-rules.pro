# Hiking Trail Navigator - ProGuard Rules

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Room entities
-keep class com.hikingtrailnavigator.app.data.local.entity.** { *; }

# Domain models (used by Gson)
-keep class com.hikingtrailnavigator.app.domain.model.** { *; }

# API models
-keep class com.hikingtrailnavigator.app.data.remote.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# OpenStreetMap (osmdroid)
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
