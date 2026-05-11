package com.example.aiddproject.home.data

import com.example.aiddproject.awarddetail.domain.AwardDetail
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.home.domain.Award
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * JVM-mockable seam over the Supabase SDK call so the repository can be unit-tested
 * without spinning up the real client. Mirrors Login's [SupabaseAuthGateway] pattern.
 */
interface SupabasePostgrestAwardsGateway {
    suspend fun listAwards(): List<Award>

    /**
     * Fetch the full payload for a single award. The [locale] arg is
     * passed through so the gateway can apply Path A (per-locale columns)
     * or Path B (`Accept-Language` header) per spec § Deferred Q6 — the
     * call site never sees which path the impl chose.
     */
    suspend fun detailAward(
        id: String,
        locale: Language,
    ): AwardDetail
}

class DefaultSupabasePostgrestAwardsGateway(
    private val supabaseClient: SupabaseClient,
) : SupabasePostgrestAwardsGateway {
    override suspend fun listAwards(): List<Award> {
        val response =
            supabaseClient.from("awards").select {
                order(column = "sort_order", order = Order.ASCENDING)
            }
        // Raw JSON parsing per Login's SupabasePostgrestUsersRepository — avoids
        // pulling kotlinx.serialization plugin annotations onto the domain entity.
        val rows = Json.parseToJsonElement(response.data).jsonArray
        return rows.map { element ->
            val obj = element.jsonObject
            Award(
                id = obj.getValue("id").jsonPrimitive.content,
                name = obj.getValue("name").jsonPrimitive.content,
                thumbnailUrl = obj["thumbnail_url"]?.jsonPrimitive?.contentOrNull,
                sortOrder = obj["sort_order"]?.jsonPrimitive?.intOrNull ?: 0,
            )
        }
    }

    /**
     * Resolved Q6 — chosen approach is **Path A** (per-locale columns):
     * the `awards` table is assumed to expose `description`,
     * `quantity_unit`, and `prize_value` as a single canonical row.
     * Until the live schema is confirmed, we project the canonical
     * column names and ignore [locale] at the SQL layer. If Path B
     * (Accept-Language header) is chosen instead, swap the `select`
     * builder to attach the header — no signature change.
     */
    override suspend fun detailAward(
        id: String,
        locale: Language,
    ): AwardDetail {
        val response =
            supabaseClient.from("awards").select {
                filter { eq("id", id) }
                limit(1)
            }
        val rows = Json.parseToJsonElement(response.data).jsonArray
        val obj =
            rows.firstOrNull()?.jsonObject
                ?: error("Award not found for id=$id")
        return AwardDetail(
            id = obj.getValue("id").jsonPrimitive.content,
            name = obj.getValue("name").jsonPrimitive.content,
            description = obj["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            quantity = obj["quantity"]?.jsonPrimitive?.intOrNull,
            quantityUnit = obj["quantity_unit"]?.jsonPrimitive?.contentOrNull,
            prizeValue = obj["prize_value"]?.jsonPrimitive?.contentOrNull,
            imageUrl = obj["image_url"]?.jsonPrimitive?.contentOrNull,
            sortOrder = obj["sort_order"]?.jsonPrimitive?.intOrNull ?: 0,
        )
    }
}

/** Default [AwardsRepository]: thin Result-wrapper over [SupabasePostgrestAwardsGateway]. */
class SupabasePostgrestAwardsRepository(
    private val gateway: SupabasePostgrestAwardsGateway,
) : AwardsRepository {
    override suspend fun list(): Result<List<Award>> = runCatching { gateway.listAwards() }

    override suspend fun detail(
        id: String,
        locale: Language,
    ): Result<AwardDetail> = runCatching { gateway.detailAward(id, locale) }
}
