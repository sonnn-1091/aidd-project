package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.KudosSummary
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface SupabaseKudosSummaryGateway {
    suspend fun fetchKudosSummary(): KudosSummary
}

class DefaultSupabaseKudosSummaryGateway(
    private val supabaseClient: SupabaseClient,
) : SupabaseKudosSummaryGateway {
    override suspend fun fetchKudosSummary(): KudosSummary {
        val response = supabaseClient.postgrest.rpc("kudos_summary")
        val parsed = Json.parseToJsonElement(response.data)
        // RPCs return a single-row table → JSON array with one element. Normalize.
        val obj: JsonObject =
            when {
                parsed is JsonObject -> parsed
                else -> parsed.jsonArray.first().jsonObject
            }
        val isKudosAvailable =
            obj
                .getValue("is_kudos_available")
                .jsonPrimitive
                .content
                .toBoolean()
        return KudosSummary(
            isKudosAvailable = isKudosAvailable,
            bannerImageUrl = obj["banner_url"]?.jsonPrimitive?.contentOrNull,
            badgeText = obj["badge_text"]?.jsonPrimitive?.contentOrNull,
            descriptionText = obj["description_text"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        )
    }
}

class SupabaseKudosSummaryRepository(
    private val gateway: SupabaseKudosSummaryGateway,
) : KudosSummaryRepository {
    override suspend fun get(): Result<KudosSummary> = runCatching { gateway.fetchKudosSummary() }
}
