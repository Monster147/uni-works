package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.isel.pdm.pokerDice.domain.Round
@Serializable
data class RoundDTO(
    @SerialName("round_number")
    val roundNumber: Int,
    val turns: List<TurnDTO>,
    @SerialName("current_player")
    val currentPlayer: UserDTO,
    val winners: List<UserDTO>
) {
    fun toRound(matchId: Int): Round {
        val turnList = turns.map { it.toTurn(roundNumber) }.toMutableList()

        return Round(
            id = roundNumber,
            matchId = matchId,
            roundNumber = roundNumber,
            turns = turnList,
            currentPlayer = currentPlayer.toUser(),
            winners = winners.map { it.toUser() }
        )
    }
}