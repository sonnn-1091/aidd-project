# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ======================================================================
# Login feature keep rules (T005)
# ======================================================================

# --- Hilt / Dagger ---
# Hilt generates code under `*_HiltModules`, `*_GeneratedInjector`, etc.
# These must survive R8 so reflection-driven injection still works.
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep,allowobfuscation class * extends androidx.lifecycle.ViewModel
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclassmembers class * {
    @dagger.hilt.* <methods>;
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# --- Kotlin coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.flow.** { *; }

# --- Ktor / supabase-kt ---
# supabase-kt uses Ktor + kotlinx.serialization. Keep @Serializable types
# and serializers; otherwise Postgrest payload (de)serialization breaks.
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# --- Credential Manager / Google ID ---
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**

# --- Compose ---
# AGP's default proguard rules already cover most of Compose, but we keep
# Composable-annotated functions explicitly so any reflection-based tooling
# in the auth flow does not trip.
-keep,includedescriptorclasses class androidx.compose.** { *; }
-dontwarn androidx.compose.**