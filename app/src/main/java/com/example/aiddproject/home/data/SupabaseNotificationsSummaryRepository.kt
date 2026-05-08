package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.NotificationsSummary
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface SupabaseNotificationsSummaryGateway {
    suspend fun fetchNotificationsSummary(): NotificationsSummary
}

class DefaultSupabaseNotificationsSummaryGateway(
    private val supabaseClient: SupabaseClient,
) : SupabaseNotificationsSummaryGateway {
    override suspend fun fetchNotificationsSummary(): NotificationsSummary {
        val response = supabaseClient.postgrest.rpc("notifications_summary")
        val parsed = Json.parseToJsonElement(response.data)
        val obj: JsonObject =
            when {
                parsed is JsonObject -> parsed
                else -> parsed.jsonArray.first().jsonObject
            }
        return NotificationsSummary(
            unreadCount = obj["unread_count"]?.jsonPrimitive?.intOrNull ?: 0,
        )
    }
}

class SupabaseNotificationsSummaryRepository(
    private val gateway: SupabaseNotificationsSummaryGateway,
) : NotificationsSummaryRepository {
    override suspend fun get(): Result<NotificationsSummary> =
        runCatching {
            gateway.fetchNotificationsSummary()
        }
}
