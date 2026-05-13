import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Login feature additions (T003)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
}

// Read Supabase keys from local.properties so they are not committed.
// Falls back to empty strings to keep the build configurable in CI where
// keys are injected via env vars.
val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) load(file.inputStream())
    }
val supabaseUrl: String =
    localProperties.getProperty("supabase.url")
        ?: System.getenv("SUPABASE_URL") ?: ""
val supabaseAnonKey: String =
    localProperties.getProperty("supabase.anonKey")
        ?: System.getenv("SUPABASE_ANON_KEY") ?: ""
// Google Cloud OAuth 2.0 *Web* client ID — required by Credential Manager's
// GetGoogleIdOption to issue an ID token Supabase will accept (T038, plan § Q7).
val googleOAuthWebClientId: String =
    localProperties.getProperty("google.oauthWebClientId")
        ?: System.getenv("GOOGLE_OAUTH_WEB_CLIENT_ID") ?: ""
// Demo mode — when true, Hilt swaps the Supabase / Google credential / users layer
// for fake implementations that always resolve as "Sunner alice@sun-asterisk.com".
// Lets engineers explore the Login → Home flow without provisioning real services.
val demoMode: Boolean =
    (localProperties.getProperty("app.demoMode") ?: System.getenv("APP_DEMO_MODE") ?: "false")
        .equals("true", ignoreCase = true)

android {
    namespace = "com.example.aiddproject"
    compileSdk {
        version =
            release(36) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        applicationId = "com.example.aiddproject"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Login feature: expose Supabase keys via BuildConfig (T003).
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField(
            "String",
            "GOOGLE_OAUTH_WEB_CLIENT_ID",
            "\"$googleOAuthWebClientId\"",
        )
        buildConfigField("boolean", "DEMO_MODE", demoMode.toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        // Login feature additions (T003)
        buildConfig = true
    }
    // Hilt testing (T005b) pulls JUnit5 jupiter jars transitively, each
    // carrying its own META-INF/LICENSE.md → merge conflict at
    // mergeDebugAndroidTestJavaResource. Exclude the duplicates.
    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

// ktlint configuration (T006). Enforces Kotlin official style on every
// `./gradlew check` per Constitution Principle I.
ktlint {
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Sun*Kudos feature — extended material icon set
    implementation(libs.androidx.compose.material.icons.extended)

    // Login feature: Supabase SDK (T003)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.android)

    // Login feature: Hilt + KSP (T003)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Login feature: Credential Manager + Google ID (T003)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Login feature: DataStore + Navigation + ViewModel-Compose (T003)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Phase 7 polish: secure logging + Play Services availability check
    implementation(libs.timber)
    implementation(libs.play.services.base)

    // Home feature: image loading for Kudos banner + countdown clock + Compose-aware lifecycle
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    // Hilt testing (T005b — resolves blocker for Slice D T019 + T044 which use
    // `createAndroidComposeRule<HiltTestActivity>()` + `@HiltAndroidTest`).
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
