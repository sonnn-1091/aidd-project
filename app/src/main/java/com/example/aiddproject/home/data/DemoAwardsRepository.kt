package com.example.aiddproject.home.data

import com.example.aiddproject.awarddetail.domain.AwardDetail
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.home.domain.Award

/** Demo build fixture mirroring the three seeded `awards` rows. */
class DemoAwardsRepository : AwardsRepository {
    override suspend fun list(): Result<List<Award>> = Result.success(DEMO_AWARDS)

    /**
     * Returns the matching seeded payload, ignoring [locale] per
     * `plan.md` § Backend Localization ("demo repository ignores the
     * locale"). Unknown ids return `Result.failure` so the screen's
     * Error state can be exercised in DEMO_MODE.
     */
    override suspend fun detail(
        id: String,
        locale: Language,
    ): Result<AwardDetail> {
        val match = DEMO_DETAILS.firstOrNull { it.id == id }
        return if (match != null) {
            Result.success(match)
        } else {
            Result.failure(NoSuchElementException("Demo: no award with id=$id"))
        }
    }

    private companion object {
        val DEMO_AWARDS: List<Award> =
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
            )

        val DEMO_DETAILS: List<AwardDetail> =
            listOf(
                AwardDetail(
                    id = "00000000-0000-0000-0000-000000000a01",
                    name = "Top Talent",
                    description =
                        "Giải thưởng Top Talent vinh danh những cá nhân xuất sắc toàn diện " +
                            "trên mọi phương diện — chuyên môn, tinh thần đóng góp, và sự lan toả " +
                            "giá trị Sun* tới đồng đội.",
                    quantity = 10,
                    quantityUnit = "Cá nhân",
                    prizeValue = "7.000.000 VNĐ",
                    // Bundled Figma badge (`ic_award_top_talent` — exported from
                    // node `6885:10293` of spec c-QM3_zjkG). Coil 2.x resolves
                    // the named-form Android resource URI; when the live
                    // `awards.image_url` ships from Supabase Storage this DEMO
                    // row goes away.
                    imageUrl = "android.resource://com.example.aiddproject/drawable/ic_award_top_talent",
                    sortOrder = 1,
                ),
                AwardDetail(
                    id = "00000000-0000-0000-0000-000000000a02",
                    name = "Top Project",
                    // Description, quantity, and quantity_unit pulled
                    // verbatim from Figma node `6885:10468` of frame
                    // `FQoJZLkG_d` ([iOS] Award_Top project). Resolves
                    // Q-TP-1 from the delta-spec: previous DEMO values
                    // (5 + "Dự án" + summary description) drifted from
                    // Figma; Figma + product copy is the source of truth.
                    description =
                        "Giải thưởng Top Project vinh danh các tập thể dự án xuất sắc với kết " +
                            "quả kinh doanh vượt kỳ vọng, hiệu quả vận hành tối ưu và tinh thần " +
                            "làm việc tận tâm. Đây là các dự án có độ phức tạp kỹ thuật cao, " +
                            "hiệu quả tối ưu hóa nguồn lực và chi phí tốt, đề xuất các ý tưởng " +
                            "có giá trị cho khách hàng, đem lại lợi nhuận vượt trội và nhận " +
                            "được phản hồi tích cực từ khách hàng. Các thành viên tuân thủ " +
                            "nghiêm ngặt các tiêu chuẩn phát triển nội bộ trong phát triển dự " +
                            "án, tạo nên một hình mẫu về sự xuất sắc và chuyên nghiệp.",
                    quantity = 2,
                    quantityUnit = "Tập thể",
                    prizeValue = "15.000.000 VNĐ",
                    imageUrl = null,
                    sortOrder = 2,
                ),
                AwardDetail(
                    id = "00000000-0000-0000-0000-000000000a03",
                    name = "Top Heart",
                    description =
                        "Top Heart tôn vinh những Sunner luôn đặt trái tim vào mỗi hành động — " +
                            "với khách hàng, với đồng nghiệp, và với cộng đồng.",
                    quantity = 8,
                    quantityUnit = "Cá nhân",
                    prizeValue = "5.000.000 VNĐ",
                    imageUrl = null,
                    sortOrder = 3,
                ),
            )
    }
}
