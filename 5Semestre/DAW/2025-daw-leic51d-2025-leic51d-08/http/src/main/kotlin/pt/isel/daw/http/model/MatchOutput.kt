package pt.isel.daw.http.model

class MatchOutput(
    val matchId: Int,
    val lobbyId: Int,
    val players: List<UserMatchOutput>,
    val rounds: List<RoundOutput>,
    val state: String,
    val winners: List<UserMatchOutput>,
    val currentRound: Int,
)
