package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.LobbyState

@Serializable
data class LobbyOutputDTO(
    val id: Int,
    val name: String,
    val description: String,
    @SerialName("nof_players")
    val nOfPlayers: String,
    val host: UserDTO,
    val players: List<UserDTO>,
    val state: LobbyState,
    val ante: Int,
    @SerialName("nof_rounds")
    val nOfRounds: Int,
    @SerialName("match_id")
    val matchId: Int,
    val auto_start_at: Long? = null,
) {
    fun toLobby(): Lobby{
        val maxPlayers = nOfPlayers.substringAfter("/").toInt()

        return Lobby(
            id = this.id,
            name = this.name,
            description = this.description,
            maxPlayers = maxPlayers,
            host = this.host.toUser(),
            rounds = this.nOfRounds,
            players = this.players.map { it.toUser() },
            ante = this.ante,
            state = this.state,
            matchId = this.matchId,
            autoStartAt = this.auto_start_at
        )
    }
}
