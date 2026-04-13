package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.isel.pdm.pokerDice.domain.Dice
import pt.isel.pdm.pokerDice.domain.DiceFace
import pt.isel.pdm.pokerDice.domain.Hand
import pt.isel.pdm.pokerDice.domain.HandCategory
import pt.isel.pdm.pokerDice.domain.Turn
import pt.isel.pdm.pokerDice.domain.TurnState
@Serializable
data class TurnDTO(
    val player: UserDTO,
    val hand: String,
    @SerialName("roll_count")
    val rollCount: Int,
    val state: String,
    val score: String?
) {
    fun toTurn(roundNumber: Int): Turn {
        val diceList = if (hand.isBlank()) {
            emptyList()
        } else {
            hand.split(", ")
                .map { DiceFace.valueOf(it.trim()) }
                .map { Dice(it) }
        }

        return Turn(
            id = 0,
            roundId = roundNumber,
            player = player.toUser(),
            hand = Hand(diceList),
            rollCount = rollCount,
            state = TurnState.valueOf(state),
            score = score?.let { HandCategory.valueOf(it) }
        )
    }
}