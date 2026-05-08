package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.Award

/**
 * Read-only repository for the awards catalog. The caller treats failure as a transport /
 * decoding error to surface in [AwardsState.Error]; an empty list maps to [AwardsState.Empty].
 */
interface AwardsRepository {
    suspend fun list(): Result<List<Award>>
}
