package com.example.aiddproject.kudos.domain

/**
 * Sunner network graph payload for the Spotlight Board (spec § US9).
 *
 * [totalKudosCount] is computed server-side and fronts the
 * "Tổng Kudos đã trao" counter at the top of the board (Q-K-2 keeps
 * the count on the graph response — no separate count endpoint). The
 * [nodes] + [edges] arrays back the pan/zoom canvas; the canvas
 * draws each [KudosEdge] as a stroked line connecting the two node
 * positions.
 */
data class SpotlightGraph(
    val nodes: List<SunnerNode>,
    val edges: List<KudosEdge>,
    val totalKudosCount: Int,
)

/**
 * Directed edge: sender → recipient. [weight] is the kudos count
 * between the pair (drives line thickness when rendered on the canvas).
 */
data class KudosEdge(
    val senderId: String,
    val recipientId: String,
    val weight: Int = 1,
)
