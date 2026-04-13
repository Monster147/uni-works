package pt.isel.pdm.pokerDice.ui.screens.game

import pt.isel.pdm.pokerDice.domain.Dice
import pt.isel.pdm.pokerDice.domain.DiceFace
import pt.isel.pdm.pokerDice.domain.Hand
import pt.isel.pdm.pokerDice.domain.HandCategory
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Turn
import pt.isel.pdm.pokerDice.domain.TurnState
import pt.isel.pdm.pokerDice.domain.User

val alice = User(1000, "Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
val bob = User(1001, "Bob", "bob@gmail.com", PasswordValidationInfo("hash"))

val aliceTurn = Turn(
    id = 1,
    roundId = 1,
    player = alice,
    hand = Hand(
        dice = listOf(
            DiceFace.NINE,
            DiceFace.TEN,
            DiceFace.JACK,
            DiceFace.QUEEN,
            DiceFace.KING
        ).map { face -> Dice(face) }
    ),
    rollCount = 1,
    state = TurnState.IN_PROGRESS,
    score = HandCategory.HIGH_CARD
)