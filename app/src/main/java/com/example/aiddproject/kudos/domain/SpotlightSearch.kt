package com.example.aiddproject.kudos.domain

/**
 * Server hit from a live spotlight search query (spec § US9).
 * Returned by [com.example.aiddproject.kudos.data.KudosRepository.searchSunner];
 * the UI debounces the input and highlights the matched node on the
 * canvas.
 */
data class SunnerMatch(
    val node: SunnerNode,
    val matchScore: Double,
)

/** Sealed result type so the canvas can render Idle/Loading/Match/NoMatch states. */
sealed interface SpotlightSearchResult {
    data object Idle : SpotlightSearchResult

    data object Loading : SpotlightSearchResult

    data class Match(
        val node: SunnerNode,
    ) : SpotlightSearchResult

    data object NoMatch : SpotlightSearchResult
}
