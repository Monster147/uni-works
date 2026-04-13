package pt.isel.pdm.pokerDice.domain

data class UserStats(
    val id: Int,
    val roundsWon: Int = 0,
    val roundsLost: Int = 0,
    val roundsDrawn: Int = 0,
    val totalMatches: Int = 0,
    val matchesWon: Int = 0,
    val matchesLost: Int = 0,
    val matchesDrawn: Int = 0,
    val winRate: Double = 0.0,
)
