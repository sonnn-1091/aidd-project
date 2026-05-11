package com.example.aiddproject.home.data

import com.example.aiddproject.awarddetail.domain.AwardDetail
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.home.domain.Award

/**
 * Read-only repository for the awards catalog.
 *
 * [list] backs Home's carousel + the Award Detail screen's category
 * dropdown — the caller treats failure as a transport / decoding error
 * to surface in [AwardsState.Error]; an empty list maps to
 * [AwardsState.Empty].
 *
 * [detail] backs the Award Detail screen body (spec `c-QM3_zjkG` § US1 +
 * § Deferred Q8 — single shared interface, one repository, two
 * methods). The [locale] arg is interpreted by the implementation per
 * the spec's Deferred Q6 (Path A: per-locale columns, Path B:
 * `Accept-Language` header). Failure maps to
 * [AwardDetailState.Error]; the [AuthRedirectController] handles
 * the 401 bounce out-of-band on the NavHost.
 */
interface AwardsRepository {
    suspend fun list(): Result<List<Award>>

    suspend fun detail(
        id: String,
        locale: Language,
    ): Result<AwardDetail>
}
