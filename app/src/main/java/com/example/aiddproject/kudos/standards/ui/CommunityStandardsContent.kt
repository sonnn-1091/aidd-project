package com.example.aiddproject.kudos.standards.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Stateless content for the Community Standards screen (Figma frame
 * `xms7csmDhD`). Hosts the Scaffold + M3 top-app-bar + the scrollable
 * body. Owns no state; all callbacks come from the caller. Mirrors the
 * `WriteKudoScreenContent` pattern so a test harness can render this
 * composable directly with a captured `onBack` lambda.
 *
 * Background chrome (full-bleed `kudos_kv_bg` + 140dp top gradient) is
 * the same convention as the Viết Kudo composer, so the top-app-bar
 * reads against a dark band even with the keyvisual underneath.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityStandardsContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(KvBaseBackground)
                .testTag(CommunityStandardsTestTags.SCREEN),
    ) {
        Image(
            painter = painterResource(R.drawable.kudos_kv_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.community_standards_title_appbar),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag(CommunityStandardsTestTags.BACK_BUTTON),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back_chevron),
                                contentDescription = stringResource(R.string.a11y_community_standards_back),
                                tint = Color.White,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = Color.White,
                            titleContentColor = Color.White,
                        ),
                    modifier = Modifier.statusBarsPadding(),
                )
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                KvBanner()
                CommunityStandardsSection()
                Divider()
                SecurityStandardsSection()
            }
        }
    }
}

@Composable
private fun KvBanner() {
    // Figma node 6885:10829 — banner sits at the TOP of the content
    // frame, fixed 151×64 within a 335-wide column. The outer Column's
    // `horizontalAlignment = CenterHorizontally` centers it like Figma.
    Image(
        painter = painterResource(R.drawable.ic_logo_root_further),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier =
            Modifier
                .width(151.dp)
                .height(64.dp)
                .testTag(CommunityStandardsTestTags.KV_BANNER),
    )
}

@Composable
private fun CommunityStandardsSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.community_standards_section_community_title),
            color = SaaCream,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.community_standards_section_community_intro),
            color = SaaCream,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.community_standards_section_community_warning),
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
        )
        CriteriaList()
    }
}

/**
 * The 10-criterion list — rendered with collection semantics so
 * TalkBack announces "List, 10 items" before reading items (spec
 * FR-006). Each row pairs a `N.` index marker with the criterion text;
 * both contribute to the row's accessible text by default.
 */
@Composable
private fun CriteriaList() {
    val criteria =
        listOf(
            R.string.community_standards_criteria_1,
            R.string.community_standards_criteria_2,
            R.string.community_standards_criteria_3,
            R.string.community_standards_criteria_4,
            R.string.community_standards_criteria_5,
            R.string.community_standards_criteria_6,
            R.string.community_standards_criteria_7,
            R.string.community_standards_criteria_8,
            R.string.community_standards_criteria_9,
            R.string.community_standards_criteria_10,
        )
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    collectionInfo = CollectionInfo(rowCount = criteria.size, columnCount = 1)
                }
                .testTag(CommunityStandardsTestTags.CRITERIA_LIST),
    ) {
        criteria.forEachIndexed { index, res ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            collectionItemInfo =
                                CollectionItemInfo(
                                    rowIndex = index,
                                    rowSpan = 1,
                                    columnIndex = 0,
                                    columnSpan = 1,
                                )
                        }
                        .testTag(CommunityStandardsTestTags.criteriaRowTag(index + 1)),
            ) {
                Text(
                    text = "${index + 1}.",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.width(24.dp),
                )
                Text(
                    text = stringResource(res),
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    Spacer(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor),
    )
}

@Composable
private fun SecurityStandardsSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.community_standards_section_security_title),
            color = SaaCream,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.community_standards_section_security_commitment),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
            )
            Text(
                text = stringResource(R.string.community_standards_section_security_info),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
            )
            Text(
                text = stringResource(R.string.community_standards_section_security_scope),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
            )
        }
        Text(
            text = stringResource(R.string.community_standards_section_security_support),
            color = SaaCream,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag(CommunityStandardsTestTags.SUPPORT_CONTACT),
        )
    }
}

// ── Figma tokens (queried 2026-05-14 via `query_section` on `6885:10832`). ───
private val KvBaseBackground: Color = Color(0xFF00070C)
private val DividerColor: Color = Color(0xFF2E3940)

@Preview(name = "Community Standards (light)", showBackground = true)
@Preview(
    name = "Community Standards (dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CommunityStandardsContentPreview() {
    CommunityStandardsContent(onBack = {})
}
