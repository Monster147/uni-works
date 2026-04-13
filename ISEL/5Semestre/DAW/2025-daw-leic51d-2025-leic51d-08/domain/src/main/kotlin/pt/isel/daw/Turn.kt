package pt.isel.daw

data class Turn(
    val id: Int,
    val roundId: Int,
    val player: User,
    val hand: Hand,
    val rollCount: Int = 0,
    var state: TurnState = TurnState.IN_PROGRESS,
    var score: HandCategory? = null,
)
