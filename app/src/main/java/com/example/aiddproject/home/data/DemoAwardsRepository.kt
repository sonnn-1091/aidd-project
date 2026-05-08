package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.Award

/** Demo build fixture mirroring the three seeded `awards` rows. */
class DemoAwardsRepository : AwardsRepository {
    override suspend fun list(): Result<List<Award>> =
        Result.success(
            listOf(
                Award(
                    id = "00000000-0000-0000-0000-000000000a01",
                    name = "Top Talent Award",
                    thumbnailUrl = null,
                    sortOrder = 1,
                ),
                Award(
                    id = "00000000-0000-0000-0000-000000000a02",
                    name = "Top Project Award",
                    thumbnailUrl = null,
                    sortOrder = 2,
                ),
                Award(
                    id = "00000000-0000-0000-0000-000000000a03",
                    name = "Top Heart Award",
                    thumbnailUrl = null,
                    sortOrder = 3,
                ),
            ),
        )
}
