package pt.isel.daw.http.model

data class LobbyInput(
    val name: String,
    val description: String,
    val max_players: Int,
    val rounds: Int,
    val ante: Int,
)
