package com.example.aiddproject.kudos.data

import com.example.aiddproject.kudos.domain.Department
import com.example.aiddproject.kudos.domain.GiftRecipient
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.domain.KudosFilter
import com.example.aiddproject.kudos.domain.KudosPage
import com.example.aiddproject.kudos.domain.PersonalStats
import com.example.aiddproject.kudos.domain.SecretBoxRef
import com.example.aiddproject.kudos.domain.SecretBoxReward
import com.example.aiddproject.kudos.domain.SpotlightGraph
import com.example.aiddproject.kudos.domain.SunnerMatch
import com.example.aiddproject.kudos.domain.SystemFlags

/**
 * Single read+write surface for the Sun*Kudos hub feature (spec
 * fO0Kt19sZZ § API Requirements). 14 suspend methods cover every
 * conceptual endpoint the hub needs; each returns [Result] so the
 * caller can branch on failure (optimistic rollback path in
 * [com.example.aiddproject.kudos.ui.KudosViewModel.onHeartTap]) without
 * exception handling at the call site.
 *
 * Production binding: `SupabaseKudosRepository` (Postgrest reads + 1
 * RPC). DEMO binding: `DemoKudosRepository` (seed fixtures only). The
 * Hilt switch lives in `KudosRepositoryModule` and follows
 * `home/data/HomeRepositoryModule`'s `BuildConfig.DEMO_MODE` pattern.
 */
interface KudosRepository {
    // Highlight + feed (US3, US4)
    suspend fun listHighlight(filter: KudosFilter): Result<List<Kudos>>

    suspend fun listKudos(
        filter: KudosFilter,
        page: Int,
        limit: Int,
    ): Result<KudosPage>

    suspend fun detail(kudosId: String): Result<Kudos>

    // Reactions (US5, Q-K-5)
    suspend fun addReaction(kudosId: String): Result<Unit>

    suspend fun removeReaction(kudosId: String): Result<Unit>

    // Filter dropdowns (US3)
    suspend fun listHashtags(): Result<List<Hashtag>>

    suspend fun listDepartments(): Result<List<Department>>

    // Spotlight (US9 + Q-K-2)
    suspend fun loadSpotlightGraph(): Result<SpotlightGraph>

    suspend fun searchSunner(
        query: String,
        limit: Int = 20,
    ): Result<List<SunnerMatch>>

    // Personal stats + system flags (US10, Q-K-1)
    suspend fun personalStats(): Result<PersonalStats>

    suspend fun systemFlags(): Result<SystemFlags>

    // Secret Box (US11)
    suspend fun nextUnopenedBox(): Result<SecretBoxRef?>

    suspend fun openSecretBox(boxId: String): Result<SecretBoxReward>

    // Top 10 (US12)
    suspend fun listRecentGiftRecipients(limit: Int = 10): Result<List<GiftRecipient>>
}
