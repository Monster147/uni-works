package pt.isel.daw.http.model

class RoundOutput(
    val roundNumber: Int,
    val turns: List<TurnOutput>,
    val currentPlayer: UserMatchOutput,
    val winners: List<UserMatchOutput>,
)
