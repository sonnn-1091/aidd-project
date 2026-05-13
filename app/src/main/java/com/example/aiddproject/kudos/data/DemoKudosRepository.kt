package com.example.aiddproject.kudos.data

import android.net.Uri
import com.example.aiddproject.kudos.compose.domain.UploadedImage
import com.example.aiddproject.kudos.compose.domain.WriteKudoDraft
import com.example.aiddproject.kudos.domain.Department
import com.example.aiddproject.kudos.domain.GiftRecipient
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.domain.KudosEdge
import com.example.aiddproject.kudos.domain.KudosFilter
import com.example.aiddproject.kudos.domain.KudosPage
import com.example.aiddproject.kudos.domain.PersonalStats
import com.example.aiddproject.kudos.domain.SecretBoxRef
import com.example.aiddproject.kudos.domain.SecretBoxReward
import com.example.aiddproject.kudos.domain.SpotlightGraph
import com.example.aiddproject.kudos.domain.SunnerMatch
import com.example.aiddproject.kudos.domain.SunnerNode
import com.example.aiddproject.kudos.domain.SystemFlags
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DEMO build fixture for the Sun*Kudos hub. Seeds 10 kudos (with
 * descending heart-counts so [listHighlight] returns a deterministic
 * top-5 ordering), 5 hashtags, 5 departments, an 8-node spotlight
 * graph carrying `totalKudosCount = 388` (matches the Figma sample),
 * canonical [PersonalStats], a single unopened Secret Box, and 8 Top
 * 10 gift recipients.
 *
 * The one piece of mutable state is the secret-box pending flag —
 * tracked via [AtomicBoolean] so the one-shot open behavior holds up
 * under concurrent test access.
 */
@Singleton
class DemoKudosRepository
    @Inject
    constructor() : KudosRepository {
        private val secretBoxPending = AtomicBoolean(true)

        /**
         * Q-W-2 partial-failure hook for the Viết Kudo composer
         * tests (T030). When set to a positive Int, the Nth call to
         * [uploadKudoImage] (1-indexed across the lifetime of this
         * instance) returns a failed Result so the test can exercise
         * the rollback path. Reset to `null` to restore the
         * always-succeeds default.
         */
        @Volatile
        var failOnNthUpload: Int? = null
        private var uploadCallCounter: Int = 0

        /** In-memory store of submitted draft IDs — used to assert no double-submit. */
        private val createdKudoIds: MutableSet<String> = mutableSetOf()

        /** In-memory record of "uploaded" storage paths for assertion in rollback tests. */
        private val uploadedPaths: MutableSet<String> = mutableSetOf()

        /** True when the test cares about whether [deleteKudoImage] was called. */
        val deletedPaths: MutableList<String> = mutableListOf()

        override suspend fun listHighlight(filter: KudosFilter): Result<List<Kudos>> {
            val filtered = applyFilter(DEMO_KUDOS, filter)
            val top5 = filtered.sortedByDescending { it.heartCount }.take(5)
            return Result.success(top5)
        }

        override suspend fun listKudos(
            filter: KudosFilter,
            page: Int,
            limit: Int,
        ): Result<KudosPage> {
            val filtered = applyFilter(DEMO_KUDOS, filter)
            val from = page * limit
            val to = (from + limit).coerceAtMost(filtered.size)
            if (from >= filtered.size) {
                return Result.success(KudosPage(items = emptyList(), hasMore = false, nextPage = null))
            }
            val slice = filtered.subList(from, to)
            val hasMore = to < filtered.size
            return Result.success(
                KudosPage(
                    items = slice,
                    hasMore = hasMore,
                    nextPage = if (hasMore) page + 1 else null,
                ),
            )
        }

        override suspend fun detail(kudosId: String): Result<Kudos> {
            val match = DEMO_KUDOS.firstOrNull { it.id == kudosId }
            return if (match != null) {
                Result.success(match)
            } else {
                Result.failure(NoSuchElementException("Demo: no kudos with id=$kudosId"))
            }
        }

        override suspend fun addReaction(kudosId: String): Result<Unit> = Result.success(Unit)

        override suspend fun removeReaction(kudosId: String): Result<Unit> = Result.success(Unit)

        override suspend fun listHashtags(): Result<List<Hashtag>> = Result.success(DEMO_HASHTAGS)

        override suspend fun listDepartments(): Result<List<Department>> = Result.success(DEMO_DEPARTMENTS)

        override suspend fun loadSpotlightGraph(): Result<SpotlightGraph> = Result.success(DEMO_SPOTLIGHT)

        override suspend fun searchSunner(
            query: String,
            limit: Int,
        ): Result<List<SunnerMatch>> {
            if (query.isBlank()) return Result.success(emptyList())
            val q = query.trim().lowercase()
            val matches =
                DEMO_SPOTLIGHT.nodes
                    .filter { it.fullName.lowercase().contains(q) }
                    .take(limit)
                    .map { SunnerMatch(node = it, matchScore = 1.0) }
            return Result.success(matches)
        }

        override suspend fun personalStats(): Result<PersonalStats> = Result.success(DEMO_STATS)

        override suspend fun systemFlags(): Result<SystemFlags> = Result.success(SystemFlags())

        override suspend fun nextUnopenedBox(): Result<SecretBoxRef?> =
            Result.success(
                if (secretBoxPending.get()) DEMO_SECRET_BOX else null,
            )

        override suspend fun openSecretBox(boxId: String): Result<SecretBoxReward> {
            if (boxId != DEMO_SECRET_BOX.id) {
                return Result.failure(NoSuchElementException("Demo: no box with id=$boxId"))
            }
            secretBoxPending.set(false)
            return Result.success(
                SecretBoxReward(
                    boxId = boxId,
                    name = "Voucher cà phê 50K",
                    description = "Một ly cà phê thân tặng từ Sun*",
                ),
            )
        }

        override suspend fun listRecentGiftRecipients(limit: Int): Result<List<GiftRecipient>> = Result.success(DEMO_RECIPIENTS.take(limit))

        // ────────────── Viết Kudo composer (7fFAb-K35a) ─────────────

        override suspend fun createKudo(draft: WriteKudoDraft): Result<Kudos> {
            if (!createdKudoIds.add(draft.id)) {
                return Result.failure(IllegalStateException("Duplicate kudoId — single-flight submit broken: ${draft.id}"))
            }
            // Synthesise sender + recipient SunnerNodes from the seed fixture
            // so the returned Kudos is shaped exactly like a real Postgrest
            // read. Recipient lookup falls back to the first non-self demo
            // user if the id doesn't match a known seed.
            val recipient =
                DEMO_KUDOS.map { it.recipient }
                    .firstOrNull { it.id == draft.recipientId }
                    ?: DEMO_KUDOS.first().recipient
            val sender = DEMO_KUDOS.first().sender
            val row =
                Kudos(
                    id = draft.id,
                    sender = sender,
                    recipient = recipient,
                    message = draft.message,
                    title = draft.title,
                    hashtags = draft.tags.map { com.example.aiddproject.kudos.domain.Hashtag(id = it, tagName = it) },
                    photos = draft.imageIds,
                    createdAt = java.time.Instant.now().toString(),
                    heartCount = 0,
                    likedByCurrentUser = false,
                    senderVisibleToMe = !draft.isAnonymous,
                    likeDisabledForMe = false,
                    isAnonymous = draft.isAnonymous,
                )
            return Result.success(row)
        }

        override suspend fun uploadKudoImage(
            kudoId: String,
            index: Int,
            uri: Uri,
        ): Result<UploadedImage> {
            uploadCallCounter += 1
            val nthToFail = failOnNthUpload
            if (nthToFail != null && uploadCallCounter == nthToFail) {
                return Result.failure(java.io.IOException("DEMO partial-failure: forced fail on upload #$uploadCallCounter"))
            }
            val path = "kudos-attachments/demo/$kudoId/${index}_${uri.lastPathSegment ?: "image"}"
            uploadedPaths += path
            return Result.success(
                UploadedImage(
                    clientId = "$kudoId-$index",
                    localUri = uri,
                    sizeBytes = 0L,
                    mime = "image/jpeg",
                    storagePath = path,
                ),
            )
        }

        override suspend fun deleteKudoImage(ref: UploadedImage): Result<Unit> {
            val path = ref.storagePath ?: return Result.success(Unit)
            uploadedPaths.remove(path)
            deletedPaths += path
            return Result.success(Unit)
        }

        private fun applyFilter(
            source: List<Kudos>,
            filter: KudosFilter,
        ): List<Kudos> {
            if (filter.hashtagId == null && filter.departmentId == null) return source
            return source.filter { kudos ->
                val tagOk =
                    filter.hashtagId == null ||
                        kudos.hashtags.any { it.id == filter.hashtagId }
                val deptOk =
                    filter.departmentId == null ||
                        kudos.sender.department?.id == filter.departmentId ||
                        kudos.recipient.department?.id == filter.departmentId
                tagOk && deptOk
            }
        }

        private companion object {
            val DEMO_DEPARTMENTS: List<Department> =
                listOf(
                    Department(id = "d01", name = "Division A"),
                    Department(id = "d02", name = "Division B"),
                    Department(id = "d03", name = "Division C"),
                    Department(id = "d04", name = "Studio X"),
                    Department(id = "d05", name = "Operations"),
                )

            val DEMO_HASHTAGS: List<Hashtag> =
                listOf(
                    Hashtag(id = "h01", tagName = "teamwork"),
                    Hashtag(id = "h02", tagName = "ownership"),
                    Hashtag(id = "h03", tagName = "innovation"),
                    Hashtag(id = "h04", tagName = "collaboration"),
                    Hashtag(id = "h05", tagName = "growth"),
                )

            val DEMO_SUNNERS: List<SunnerNode> =
                listOf(
                    SunnerNode(id = "u01", fullName = "Nguyễn An", starTier = 3, department = DEMO_DEPARTMENTS[0]),
                    SunnerNode(id = "u02", fullName = "Trần Bình", starTier = 2, department = DEMO_DEPARTMENTS[0]),
                    SunnerNode(id = "u03", fullName = "Lê Châu", starTier = 1, department = DEMO_DEPARTMENTS[1]),
                    SunnerNode(id = "u04", fullName = "Phạm Dương", starTier = 2, department = DEMO_DEPARTMENTS[1]),
                    SunnerNode(id = "u05", fullName = "Hồ Em", starTier = 0, department = DEMO_DEPARTMENTS[2]),
                    SunnerNode(id = "u06", fullName = "Đỗ Phong", starTier = 1, department = DEMO_DEPARTMENTS[2]),
                    SunnerNode(id = "u07", fullName = "Bùi Giang", starTier = 0, department = DEMO_DEPARTMENTS[3]),
                    SunnerNode(id = "u08", fullName = "Vũ Hà", starTier = 1, department = DEMO_DEPARTMENTS[4]),
                )

            val DEMO_KUDOS: List<Kudos> =
                listOf(
                    kudos("k01", from = 0, to = 1, hearts = 42, tags = listOf(0, 1), title = "Cảm ơn vì đã support team"),
                    kudos("k02", from = 2, to = 3, hearts = 38, tags = listOf(0), anonymous = true),
                    kudos("k03", from = 4, to = 5, hearts = 31, tags = listOf(2)),
                    kudos("k04", from = 6, to = 7, hearts = 27, tags = listOf(3, 4)),
                    kudos("k05", from = 1, to = 2, hearts = 23, tags = listOf(0, 2)),
                    kudos("k06", from = 3, to = 4, hearts = 19, tags = listOf(1)),
                    kudos("k07", from = 5, to = 6, hearts = 16, tags = listOf(4)),
                    kudos("k08", from = 7, to = 0, hearts = 12, tags = listOf(3)),
                    kudos("k09", from = 0, to = 4, hearts = 9, tags = listOf(0, 3), anonymous = true),
                    kudos("k10", from = 2, to = 7, hearts = 5, tags = listOf(2, 4)),
                )

            val DEMO_SPOTLIGHT: SpotlightGraph =
                SpotlightGraph(
                    nodes = DEMO_SUNNERS,
                    edges =
                        listOf(
                            KudosEdge(senderId = "u01", recipientId = "u02", weight = 4),
                            KudosEdge(senderId = "u03", recipientId = "u04", weight = 3),
                            KudosEdge(senderId = "u05", recipientId = "u06", weight = 2),
                            KudosEdge(senderId = "u07", recipientId = "u08", weight = 1),
                            KudosEdge(senderId = "u02", recipientId = "u03", weight = 2),
                            KudosEdge(senderId = "u04", recipientId = "u05", weight = 1),
                            KudosEdge(senderId = "u06", recipientId = "u07", weight = 1),
                            KudosEdge(senderId = "u08", recipientId = "u01", weight = 2),
                        ),
                    totalKudosCount = 388,
                )

            val DEMO_STATS: PersonalStats =
                PersonalStats(
                    kudosReceived = 42,
                    kudosSent = 18,
                    heartsReceived = 156,
                    secretBoxesOpened = 3,
                    secretBoxesUnopened = 2,
                )

            val DEMO_SECRET_BOX: SecretBoxRef = SecretBoxRef(id = "sb01")

            val DEMO_RECIPIENTS: List<GiftRecipient> =
                listOf(
                    GiftRecipient(userId = "u01", fullName = "Nguyễn An", rewardName = "Voucher 100K"),
                    GiftRecipient(userId = "u02", fullName = "Trần Bình", rewardName = "Áo hoodie Sun*"),
                    GiftRecipient(userId = "u03", fullName = "Lê Châu", rewardName = "Túi tote"),
                    GiftRecipient(userId = "u04", fullName = "Phạm Dương", rewardName = "Combo cà phê"),
                    GiftRecipient(userId = "u05", fullName = "Hồ Em", rewardName = "Voucher 200K"),
                    GiftRecipient(userId = "u06", fullName = "Đỗ Phong", rewardName = "Sticker pack"),
                    GiftRecipient(userId = "u07", fullName = "Bùi Giang", rewardName = "Sổ tay Sun*"),
                    GiftRecipient(userId = "u08", fullName = "Vũ Hà", rewardName = "Mũ Sun*"),
                )

            private fun kudos(
                id: String,
                from: Int,
                to: Int,
                hearts: Int,
                tags: List<Int>,
                title: String? = null,
                anonymous: Boolean = false,
            ): Kudos {
                val sender = DEMO_SUNNERS[from]
                val recipient = DEMO_SUNNERS[to]
                return Kudos(
                    id = id,
                    sender = sender,
                    recipient = recipient,
                    message =
                        "Đây là nội dung Kudos mẫu cho ${recipient.fullName}. " +
                            "Cảm ơn bạn đã đồng hành cùng team trong dự án vừa qua, " +
                            "tinh thần làm việc và sự tận tâm của bạn là nguồn cảm hứng lớn.",
                    title = title,
                    hashtags = tags.map { DEMO_HASHTAGS[it] },
                    photos = emptyList(),
                    createdAt = "2026-05-12T10:00:00Z",
                    heartCount = hearts,
                    likedByCurrentUser = false,
                    senderVisibleToMe = !anonymous,
                    likeDisabledForMe = false,
                    anonymousNickname = if (anonymous) "Người bí ẩn" else null,
                    isAnonymous = anonymous,
                )
            }
        }
    }
