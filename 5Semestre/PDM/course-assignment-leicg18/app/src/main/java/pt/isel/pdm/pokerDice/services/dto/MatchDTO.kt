package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.MatchState
import pt.isel.pdm.pokerDice.domain.Round

@Serializable
data class MatchDTO(
    @SerialName("match_id")
    val matchId: Int,
    @SerialName("lobby_id")
    val lobbyId: Int,
    val players: List<UserDTO>,
    val rounds: List<RoundDTO>,
    val state: String,
    val winners: List<UserDTO>,
    @SerialName("current_round")
    val currentRound: Int
)  {
    fun toMatch(): Match {
        return Match(
            id = this.matchId,
            lobbyId = this.lobbyId,
            players = this.players.map { it.toUser() },
            rounds = mutableListOf<Round>().apply { rounds.sortedBy { it.roundNumber }.forEach { add(it.toRound(matchId)) } },
            currentRound = this.currentRound,
            state = MatchState.valueOf(this.state),
            winners = winners.map { it.toUser() }
        )
    }
}
