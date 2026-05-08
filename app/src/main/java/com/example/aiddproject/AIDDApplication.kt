package com.example.aiddproject

import android.app.Application
import com.example.aiddproject.core.logging.SecureTimberTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AIDDApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Phase 7 polish (T068): plant a token-scrubbing Timber tree on debug only.
        // Release builds intentionally plant no tree so verbose logs are stripped.
        if (BuildConfig.DEBUG) {
            Timber.plant(SecureTimberTree())
        }
    }
}
