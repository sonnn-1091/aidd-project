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
                Award(
                    id = "00000000-0000-0000-0000-000000000a04",
                    name = "Top Project Leader Award",
                    thumbnailUrl = null,
                    sortOrder = 4,
                ),
                Award(
                    id = "00000000-0000-0000-0000-000000000a05",
                    name = "Best Manager Award",
                    thumbnailUrl = null,
                    sortOrder = 5,
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
                    // Bundled Figma badge — BG (160×160) composited offline
                    // with the Top Project wordmark (106×16) centered, per
                    // Picture-Award INSTANCE `6885:10463` layout (flex
                    // column, padding 71.364px top + 25.455px sides). Mirrors
                    // Top Talent's resource URI approach; will be replaced by
                    // a live `awards.image_url` once Supabase Storage ships.
                    imageUrl = "android.resource://com.example.aiddproject/drawable/ic_award_top_project",
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
                AwardDetail(
                    id = "00000000-0000-0000-0000-000000000a04",
                    name = "Top Project Leader",
                    // Description pulled verbatim from Figma node `6885:10542`
                    // of frame `QQvsfK3yaK` ([iOS] Award_Top project leader),
                    // per delta-spec § Data Requirements. No new Q-numbers —
                    // the shipped Q-TP-2 `%02d` formatter renders the
                    // single-digit quantity as `03 Cá nhân` automatically.
                    description =
                        "Giải thưởng Top Project Leader vinh danh những nhà quản lý dự án xuất " +
                            "sắc – những người hội tụ năng lực quản lý vững vàng, khả năng truyền " +
                            "cảm hứng mạnh mẽ, và tư duy “Aim High – Be Agile” trong mọi bài toán " +
                            "và bối cảnh. Dưới sự dẫn dắt của họ, các thành viên không chỉ cùng " +
                            "nhau vượt qua thử thách và đạt được mục tiêu đề ra, mà còn giữ vững " +
                            "ngọn lửa nhiệt huyết, tinh thần Wasshoi, và trưởng thành để trở " +
                            "thành phiên bản tinh hoa – hạnh phúc hơn của chính mình.",
                    quantity = 3,
                    quantityUnit = "Cá nhân",
                    prizeValue = "7.000.000 VNĐ",
                    // Bundled Figma badge — BG (160×160, same hash as Top
                    // Project) composited offline with the Top Project Leader
                    // wordmark (111×31, two lines) centered, per Picture-Award
                    // INSTANCE `6885:10537` layout.
                    imageUrl = "android.resource://com.example.aiddproject/drawable/ic_award_top_project_leader",
                    sortOrder = 4,
                ),
                AwardDetail(
                    id = "00000000-0000-0000-0000-000000000a05",
                    name = "Best Manager",
                    // Description pulled verbatim from Figma node `6885:10616`
                    // of frame `7y195PPTxQ` ([iOS] Award_Best Manager), per
                    // delta-spec § Data Requirements. quantity=1 renders as
                    // "01" via the shipped Q-TP-2 %02d formatter.
                    description =
                        "Giải thưởng Best Manager vinh danh những nhà lãnh đạo tiêu biểu – " +
                            "người đã dẫn dắt đội ngũ của mình tạo ra kết quả vượt kỳ vọng, " +
                            "tác động nổi bật đến hiệu quả kinh doanh và sự phát triển bền " +
                            "vững của tổ chức. Dưới sự lãnh đạo của họ, đội ngũ luôn chinh " +
                            "phục và làm chủ mọi mục tiêu bằng năng lực đa nhiệm, khả năng " +
                            "phối hợp hiệu quả, và tư duy ứng dụng công nghệ linh hoạt trong " +
                            "kỷ nguyên số. Họ truyền cảm hứng để tập thể trở nên tự tin tràn " +
                            "đầy năng lượng, sẵn sàng đón nhận, thậm chí dẫn dắt tạo ra " +
                            "những thay đổi có tính cách mạng.",
                    quantity = 1,
                    quantityUnit = "Cá nhân",
                    prizeValue = "10.000.000 VNĐ",
                    // Bundled Figma badge — BG (160×160, shared hash) +
                    // single-line "BEST MANAGER" wordmark (111×15) centered,
                    // per Picture-Award INSTANCE `6885:10611` layout.
                    imageUrl = "android.resource://com.example.aiddproject/drawable/ic_award_best_manager",
                    sortOrder = 5,
                ),
            )
    }
}
