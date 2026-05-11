package com.example.aiddproject.awarddetail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.R
import com.example.aiddproject.awarddetail.domain.states.AwardDetailState
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguagePreferenceRepository
import com.example.aiddproject.home.data.AwardsRepository
import com.example.aiddproject.home.data.NotificationsSummaryRepository
import com.example.aiddproject.home.domain.states.AwardsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Hilt-injected ViewModel for `AwardDetailScreen` (spec `c-QM3_zjkG`).
 *
 * Init coroutine implements the FR-001 + Resolved Q1 routine:
 * 1. Read `awardId` from [SavedStateHandle] (auto-populated from the
 *    nav argument).
 * 2. If non-null, call `repository.detail(id, locale)` and emit
 *    [AwardDetailState.Loaded] / [AwardDetailState.Error].
 * 3. If null (e.g. entered via bottom-nav Awards tab without prior
 *    selection), call `repository.list()`, pick the first id by
 *    `sort_order`, write it back to [SavedStateHandle], then fetch
 *    the detail.
 *
 * The categories list is loaded **eagerly** alongside the body so the
 * dropdown (Phase 4) is ready as soon as US2 wires it. The bell badge
 * count is fetched via the existing `NotificationsSummaryRepository`
 * (same source as Home).
 *
 * Telemetry: emits `award_detail.entered`, `award_detail.retry`, and
 * (Phase 4) `award_detail.category_changed` breadcrumbs through
 * `Timber.tag("AwardDetailTelemetry")`. The existing `SecureTimberTree`
 * scrub from Home Phase 10 strips any token-shaped values.
 */
@HiltViewModel
class AwardDetailViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val awardsRepository: AwardsRepository,
        private val notificationsRepository: NotificationsSummaryRepository,
        languageRepository: LanguagePreferenceRepository,
    ) : ViewModel() {
        private val activeAwardIdState =
            MutableStateFlow<String?>(savedStateHandle[NAV_ARG_AWARD_ID])
        private val detailState = MutableStateFlow<AwardDetailState>(AwardDetailState.Loading)
        private val categoriesState = MutableStateFlow<AwardsState>(AwardsState.Loading)
        private val unreadCountState = MutableStateFlow(0)
        private val languageState: StateFlow<Language> =
            languageRepository.language.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = Language.Default,
            )

        val uiState: StateFlow<AwardDetailUiState> =
            combine(
                activeAwardIdState,
                detailState,
                categoriesState,
                unreadCountState,
                languageState,
            ) { activeId, detail, categories, unread, language ->
                AwardDetailUiState(activeId, detail, categories, unread, language)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = AwardDetailUiState.Empty,
            )

        private var detailJob: Job? = null

        init {
            Timber
                .tag(TELEMETRY_TAG)
                .i("award_detail.entered awardId=%s", activeAwardIdState.value)
            viewModelScope.launch { loadCategories() }
            viewModelScope.launch { loadNotifications() }
            viewModelScope.launch { resolveAwardIdAndLoadDetail() }
        }

        /** Re-issues the detail fetch for the current [activeAwardIdState]. */
        fun onRetry() {
            Timber.tag(TELEMETRY_TAG).i("award_detail.retry")
            val current = activeAwardIdState.value
            if (current != null) {
                viewModelScope.launch { loadDetail(current) }
            } else {
                viewModelScope.launch { resolveAwardIdAndLoadDetail() }
            }
        }

        private suspend fun resolveAwardIdAndLoadDetail() {
            val current = activeAwardIdState.value
            if (current != null) {
                loadDetail(current)
                return
            }
            // FR-001 + Resolved Q1: fall back to the first award by sort_order.
            awardsRepository.list().fold(
                onSuccess = { awards ->
                    val first = awards.firstOrNull()
                    if (first == null) {
                        // Empty catalogue — surface as Error so the user sees a
                        // friendly message and Retry button (FR-004).
                        detailState.value =
                            AwardDetailState.Error(R.string.award_detail_error)
                        return
                    }
                    activeAwardIdState.value = first.id
                    savedStateHandle[NAV_ARG_AWARD_ID] = first.id
                    loadDetail(first.id)
                },
                onFailure = {
                    detailState.value =
                        AwardDetailState.Error(R.string.award_detail_error)
                },
            )
        }

        private suspend fun loadDetail(awardId: String) {
            detailJob?.cancel()
            detailState.value = AwardDetailState.Loading
            val job =
                viewModelScope.launch {
                    awardsRepository.detail(awardId, languageState.value).fold(
                        onSuccess = { detailState.value = AwardDetailState.Loaded(it) },
                        onFailure = {
                            detailState.value =
                                AwardDetailState.Error(R.string.award_detail_error)
                        },
                    )
                }
            detailJob = job
            job.join()
        }

        private suspend fun loadCategories() {
            awardsRepository.list().fold(
                onSuccess = { awards ->
                    categoriesState.value =
                        if (awards.isEmpty()) {
                            AwardsState.Empty
                        } else {
                            AwardsState.Populated(awards)
                        }
                },
                onFailure = {
                    categoriesState.value = AwardsState.Error(message = null)
                },
            )
        }

        private suspend fun loadNotifications() {
            notificationsRepository.get().fold(
                onSuccess = { summary -> unreadCountState.value = summary.unreadCount },
                onFailure = { /* swallow — bell badge is non-critical, mirrors Home */ },
            )
        }

        private companion object {
            const val NAV_ARG_AWARD_ID: String = "awardId"
            const val TELEMETRY_TAG: String = "AwardDetailTelemetry"
        }
    }
