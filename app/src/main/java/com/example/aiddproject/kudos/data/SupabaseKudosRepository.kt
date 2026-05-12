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
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production Supabase Postgrest binding for [KudosRepository].
 *
 * Scaffold only — each method throws `NotImplementedError` until the
 * backend tables (`kudos`, `kudos_hashtags`, `hashtags`,
 * `departments`, `reactions`, `user_stats`, `secret_boxes`,
 * `reward_recipients`, `system_flags`, `spotlight_graph`) ship with
 * RLS policies. The Hilt graph binds this only when
 * `BuildConfig.DEMO_MODE = false`; until backend lands, dev/QA runs
 * on DEMO_MODE = true (the canonical project pattern).
 */
@Singleton
class SupabaseKudosRepository
    @Inject
    constructor(
        @Suppress("unused") private val supabaseClient: SupabaseClient,
    ) : KudosRepository {
        override suspend fun listHighlight(filter: KudosFilter): Result<List<Kudos>> =
            TODO("Supabase impl — Postgrest read `kudos` filtered by hashtag + department, sorted by heart_count DESC, limit 5")

        override suspend fun listKudos(
            filter: KudosFilter,
            page: Int,
            limit: Int,
        ): Result<KudosPage> = TODO("Supabase impl — Postgrest paginated read with hasMore + nextPage cursor")

        override suspend fun detail(kudosId: String): Result<Kudos> = TODO("Supabase impl — Postgrest single-row read by id")

        override suspend fun addReaction(kudosId: String): Result<Unit> =
            TODO("Supabase impl — Postgrest insert into reactions; unique(user_id, kudos_id) enforces idempotency")

        override suspend fun removeReaction(kudosId: String): Result<Unit> =
            TODO("Supabase impl — Postgrest delete from reactions where user_id=auth.uid() and kudos_id=:id")

        override suspend fun listHashtags(): Result<List<Hashtag>> = TODO("Supabase impl — Postgrest read `hashtags`")

        override suspend fun listDepartments(): Result<List<Department>> = TODO("Supabase impl — Postgrest read `departments`")

        override suspend fun loadSpotlightGraph(): Result<SpotlightGraph> =
            TODO("Supabase impl — RPC `load_spotlight_graph()` returning nodes + edges + total_kudos_count")

        override suspend fun searchSunner(
            query: String,
            limit: Int,
        ): Result<List<SunnerMatch>> = TODO("Supabase impl — RPC `search_sunner(:q, :limit)` returning fuzzy matches")

        override suspend fun personalStats(): Result<PersonalStats> = TODO("Supabase impl — Postgrest read `user_stats` view")

        override suspend fun systemFlags(): Result<SystemFlags> = TODO("Supabase impl — Postgrest read `system_flags` singleton row")

        override suspend fun nextUnopenedBox(): Result<SecretBoxRef?> =
            TODO("Supabase impl — Postgrest read `secret_boxes` where user_id=auth.uid() and opened_at is null limit 1")

        override suspend fun openSecretBox(boxId: String): Result<SecretBoxReward> =
            TODO("Supabase impl — RPC `open_secret_box(:id)` returns reward + flips opened_at in a single tx")

        override suspend fun listRecentGiftRecipients(limit: Int): Result<List<GiftRecipient>> =
            TODO("Supabase impl — Postgrest read `reward_recipients` order by awarded_at DESC limit :limit")
    }
