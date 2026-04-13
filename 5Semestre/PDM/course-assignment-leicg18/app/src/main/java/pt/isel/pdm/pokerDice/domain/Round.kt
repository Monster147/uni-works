package pt.isel.pdm.pokerDice.domain

data class Round(
    val id: Int,
    val matchId: Int,
    val roundNumber: Int,
    val turns: MutableList<Turn>,
    val currentPlayer: User,
    val winners: List<User> = emptyList()
)
