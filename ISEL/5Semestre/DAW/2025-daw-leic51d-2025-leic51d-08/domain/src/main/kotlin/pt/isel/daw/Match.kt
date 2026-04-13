package pt.isel.daw

data class Match(
    val id: Int,
    val lobbyId: Int,
    val players: List<User>,
    val rounds: List<Round>,
    var currentRound: Int,
    var state: MatchState = MatchState.IN_PROGRESS,
    val winners: List<User> = emptyList(),
)
