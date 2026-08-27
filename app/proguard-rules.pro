# Start.io SDK ProGuard / R8 Rules
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod
-dontwarn com.startapp.**
-keep class com.startapp.** {
      *;
}
-keep class com.startapp.sdk.ads.banner.Banner {
      *;
}
-keep class com.startapp.sdk.adsbase.** {
      *;
}

# Kotlin Coroutines and Serialization
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-keep class com.adspay.app.data.models.** { *; }
