package com.example.aiddproject.awarddetail.domain

/**
 * Full payload for a single award category, rendered by the
 * `[iOS] Award_Top talent` screen (spec `c-QM3_zjkG`).
 *
 * Sibling to [com.example.aiddproject.home.domain.Award], which is the
 * trimmed list-model used by Home's carousel. The two share `id` +
 * `name` + `sortOrder` but the detail screen needs the description,
 * recipient count + unit, prize value, and badge image URL on top.
 *
 * Field rules (from spec § Key Entities + Resolved Q4/Q5):
 * - `prize_value` is **pre-formatted** for display (e.g.
 *   `"7.000.000 VNĐ"`) — the client renders it verbatim with no
 *   locale-aware number formatting (Resolved Q5).
 * - `quantity`, `quantity_unit`, `prize_value`, `image_url` are
 *   nullable; the UI falls back to a placeholder when any is `null`
 *   (FR-008 + TC_IOS_AWARD_DETAIL_FUN_004 / FUN_020).
 * - Localization (Resolved Q6 deferred) — `name`, `description`,
 *   `quantity_unit` carry whichever locale the backend serves; the
 *   client MUST NOT translate API strings.
 */
data class AwardDetail(
    val id: String,
    val name: String,
    val description: String,
    val quantity: Int? = null,
    val quantityUnit: String? = null,
    val prizeValue: String? = null,
    // Optional override for the prize-row caption. When `null`, the UI
    // falls back to the localized default ("cho mỗi giải thưởng").
    // Set explicitly when an award uses a different per-row caption —
    // e.g., MVP shows "cho giải cá nhân" (delta-spec b2BuS8HYIt
    // Q-MVP-1, 2026-05-11).
    val prizeCaption: String? = null,
    // Optional second prize row for awards with both individual and
    // team payouts — e.g., Signature 2025 — Creator ships 5.000.000 VNĐ
    // for "cá nhân" + 8.000.000 VNĐ for "tập thể" (delta-spec
    // O98TwiHaJe Q-SIG-1, 2026-05-11). When both `prizeValueTeam`
    // and `prizeCaptionTeam` are non-null, the UI renders a second
    // prize-value row below the first.
    val prizeValueTeam: String? = null,
    val prizeCaptionTeam: String? = null,
    val imageUrl: String? = null,
    val sortOrder: Int = 0,
)
