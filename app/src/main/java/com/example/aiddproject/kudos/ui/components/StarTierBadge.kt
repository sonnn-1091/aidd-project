package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Star tier glyph row (spec § US5 scenario 6).
 *
 * `tier = 0` returns nothing (caller should branch). `tier = 1..3`
 * renders that many filled stars side-by-side.
 */
@Composable
fun StarTierBadge(
    tier: Int,
    modifier: Modifier = Modifier,
) {
    if (tier <= 0) return
    Row(modifier = modifier.testTag(KudosTestTags.STAR_TIER_BADGE)) {
        repeat(tier.coerceAtMost(3)) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = SaaCream,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
