package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.KudosSummary

/** Demo build fixture mirroring the seeded singleton `kudos_settings` row. */
class DemoKudosSummaryRepository : KudosSummaryRepository {
    override suspend fun get(): Result<KudosSummary> =
        Result.success(
            KudosSummary(
                isKudosAvailable = true,
                bannerImageUrl = null,
                badgeText = "FUN",
                descriptionText = "Trao những lời cảm ơn tới đồng nghiệp đã đồng hành cùng bạn.",
            ),
        )
}
