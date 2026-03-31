# Keep data model classes (used with Gson)
-keep class com.gkk.mppsc.data.models.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keepattributes EnclosingMethod
-keep class sun.misc.Unsafe { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-dontwarn androidx.compose.**
