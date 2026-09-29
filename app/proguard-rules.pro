# ProGuard & R8 Optimization Rules for OmniCart

# 1. WebView & JavaScript Bridge (Required for OmniMapView Leaflet/OSM JS interface)
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.example.ui.components.MapBridge { *; }

# 2. Line numbers and stack trace preservation
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# 3. Room Database & Local Cache Entities
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract <methods>;
}
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.example.model.** { *; }
-keep class com.example.data.local.** { *; }

# 4. Moshi & JSON Serialization
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-dontwarn com.squareup.moshi.**

# 5. OkHttp & Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# 6. Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-dontwarn kotlinx.coroutines.**

# 7. Coil Image Loading
-dontwarn coil.**
-keep class coil.** { *; }

# 8. Google Play Services & Maps
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.maps.**
