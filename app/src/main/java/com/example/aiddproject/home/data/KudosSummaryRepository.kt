package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.KudosSummary

/** RPC-backed read of the singleton `kudos_settings` row plus its feature flag. */
interface KudosSummaryRepository {
    suspend fun get(): Result<KudosSummary>
}
