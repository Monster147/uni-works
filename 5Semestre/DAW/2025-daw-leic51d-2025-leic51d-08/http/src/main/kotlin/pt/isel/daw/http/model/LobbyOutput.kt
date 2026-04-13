package pt.isel.daw.http.model

import pt.isel.daw.LobbyState
import pt.isel.daw.User

class LobbyOutput(
    val id: Int,
    val name: String,
    val description: String,
    val nOfPlayers: String,
    val host: User,
    val players: List<User>,
    val state: LobbyState,
    val ante: Int,
    val nOfRounds: Int,
    val matchId: Int,
    val autoStartAt: Long?,
)
