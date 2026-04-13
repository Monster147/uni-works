package pt.isel.daw.http.model

class TurnOutput(
    val player: UserMatchOutput,
    val hand: String,
    val rollCount: Int,
    val state: String,
    val score: String?,
)
