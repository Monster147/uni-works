package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.Serializable
import pt.isel.pdm.pokerDice.domain.UserStats

@Serializable
data class UserStatsDTO(
    val id: Int,
    val rounds_won: Int,
    val rounds_lost: Int,
    val rounds_drawn: Int,
    val total_matches: Int,
    val matches_won: Int,
    val matches_lost: Int,
    val matches_drawn: Int,
    val winrate: Double,
) {
    fun toUserStats(): UserStats =
        UserStats(
            id = this.id,
            roundsWon = this.rounds_won,
            roundsLost = this.rounds_lost,
            roundsDrawn = this.rounds_drawn,
            totalMatches = this.total_matches,
            matchesWon = this.matches_won,
            matchesLost = this.matches_lost,
            matchesDrawn = this.matches_drawn,
            winRate = this.winrate
        )
}
