package com.example.aiddproject.home.data

import com.example.aiddproject.awarddetail.domain.AwardDetail
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.home.domain.Award
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression guards for the Q-TP-1 fix in [DemoAwardsRepository] —
 * the Top Project DEMO payload was previously drifting from Figma
 * node `6885:10468` (`quantity=5`, `quantityUnit="Dự án"`, summary
 * description). Commit `daaf526` aligned it; this test pins the
 * values so a future copy edit has to update the test too.
 *
 * The repository itself is intentionally a static fake — these
 * tests double as documentation of the expected DEMO shape for
 * other tests that depend on the fixture.
 *
 * Pinned 2026-05-11 per delta-spec `FQoJZLkG_d` Q-TP-1 resolution.
 */
class DemoAwardsRepositoryTest {
    private val repository = DemoAwardsRepository()

    @Test
    fun `detail returns top project payload matching figma node 6885 10468`() =
        runTest {
            val result = repository.detail("00000000-0000-0000-0000-000000000a02", Language.VN)

            val expected =
                AwardDetail(
                    id = "00000000-0000-0000-0000-000000000a02",
                    name = "Top Project",
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
                    imageUrl = "android.resource://com.example.aiddproject/drawable/ic_award_top_project",
                    sortOrder = 2,
                )
            assertEquals(Result.success(expected), result)
        }

    @Test
    fun `detail returns top talent payload unchanged`() =
        runTest {
            val result = repository.detail("00000000-0000-0000-0000-000000000a01", Language.VN)

            val detail = result.getOrNull()!!
            assertEquals("Top Talent", detail.name)
            assertEquals(10, detail.quantity)
            assertEquals("Cá nhân", detail.quantityUnit)
            assertEquals("7.000.000 VNĐ", detail.prizeValue)
            assertEquals(
                "android.resource://com.example.aiddproject/drawable/ic_award_top_talent",
                detail.imageUrl,
            )
            assertEquals(1, detail.sortOrder)
        }

    @Test
    fun `detail returns top heart payload unchanged`() =
        runTest {
            val result = repository.detail("00000000-0000-0000-0000-000000000a03", Language.VN)

            val detail = result.getOrNull()!!
            assertEquals("Top Heart", detail.name)
            assertEquals(8, detail.quantity)
            assertEquals("Cá nhân", detail.quantityUnit)
            assertEquals("5.000.000 VNĐ", detail.prizeValue)
            assertEquals(3, detail.sortOrder)
        }

    @Test
    fun `detail returns failure when id unknown`() =
        runTest {
            val result = repository.detail("nonexistent-id", Language.VN)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NoSuchElementException)
        }

    @Test
    fun `list returns three demo awards sorted by sort order`() =
        runTest {
            val result = repository.list()

            val awards = result.getOrNull()!!
            assertEquals(3, awards.size)
            assertEquals(
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
                awards,
            )
        }
}
