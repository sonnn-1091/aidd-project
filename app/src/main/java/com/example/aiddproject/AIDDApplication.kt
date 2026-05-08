package com.example.aiddproject

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.aiddproject.core.logging.SecureTimberTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AIDDApplication :
    Application(),
    ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Phase 7 polish (T068): plant a token-scrubbing Timber tree on debug only.
        // Release builds intentionally plant no tree so verbose logs are stripped.
        if (BuildConfig.DEBUG) {
            Timber.plant(SecureTimberTree())
        }
    }

    /**
     * Singleton Coil [ImageLoader] used by every `AsyncImage` (Q-Plan-1).
     *
     * Default behaviour:
     *  - Public CDN — no auth interceptor, no `Authorization` header on banner
     *    requests.
     *  - Default disk + memory cache enabled (banner is a non-sensitive brand
     *    asset, so caching is safe).
     *  - `crossfade` on so swapping from the local fallback to the remote
     *    banner is a soft fade rather than a flash.
     *
     * FORWARD-COMPAT: if backend later moves the Kudos banner to Supabase
     * Storage, swap in an OkHttpClient with an interceptor that injects
     * `Authorization: Bearer <accessToken>` from `SessionRepository` AND
     * disable the disk cache (`diskCachePolicy(CachePolicy.DISABLED)`).
     * Only this builder changes — no Home composables or callsites need
     * touching.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .crossfade(true)
            .build()
}
