package com.example.aiddproject.home.data

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
}

/** Default [AwardsRepository]: thin Result-wrapper over [SupabasePostgrestAwardsGateway]. */
class SupabasePostgrestAwardsRepository(
    private val gateway: SupabasePostgrestAwardsGateway,
) : AwardsRepository {
    override suspend fun list(): Result<List<Award>> = runCatching { gateway.listAwards() }
}
