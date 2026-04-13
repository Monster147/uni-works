package pt.isel.pdm.pokerDice.ui.screens.game

import pt.isel.pdm.pokerDice.domain.Dice
import pt.isel.pdm.pokerDice.domain.DiceFace
import pt.isel.pdm.pokerDice.domain.Hand
import pt.isel.pdm.pokerDice.domain.HandCategory
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.MatchState
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Round
import pt.isel.pdm.pokerDice.domain.Turn
import pt.isel.pdm.pokerDice.domain.TurnState
import pt.isel.pdm.pokerDice.domain.User

val alice = User(1000, "Alice", "alice@gmail.com", PasswordValidationInfo("hash"))
val bob = User(1001, "Bob", "bob@gmail.com", PasswordValidationInfo("hash"))

val aliceTurn1NoRoll = Turn(
    id = 1,
    roundId = 1,
    player = alice,
    hand = Hand(),
    rollCount = 0,
    state = TurnState.IN_PROGRESS,
    score = null
)

val aliceTurn1 = Turn(
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

val aliceWinner = Turn(
    id = 1,
    roundId = 1,
    player = alice,
    hand = Hand(dice = List(5) { Dice(DiceFace.ACE) }),
    rollCount = 0,
    state = TurnState.COMPLETED,
    score = HandCategory.FIVE_OF_A_KIND
)

val bobTurn1 = Turn(
    id = 2,
    roundId = 1,
    player = bob,
    hand = Hand(), // ainda não jogou
    rollCount = 0,
    state = TurnState.IN_PROGRESS,
    score = null
)

val bobTurnWaiting = Turn(
    id = 2,
    roundId = 1,
    player = bob,
    hand = Hand(
        dice = listOf(
            DiceFace.NINE,
            DiceFace.TEN,
            DiceFace.JACK,
            DiceFace.QUEEN,
            DiceFace.KING
        ).map { Dice(it) }
    ),
    rollCount = 1,
    state = TurnState.IN_PROGRESS,
    score = null
)

val bobLoser = Turn(
    id = 2,
    roundId = 1,
    player = bob,
    hand = Hand(
        dice = listOf(
            DiceFace.NINE,
            DiceFace.TEN,
            DiceFace.JACK,
            DiceFace.QUEEN,
            DiceFace.KING
        ).map { face -> Dice(face) }
    ),
    rollCount = 0,
    state = TurnState.COMPLETED,
    score = HandCategory.HIGH_CARD
)

val aliceTurn2 = Turn(
    id = 3,
    roundId = 2,
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
    score = null
)
val bobTurn2 = Turn(
    id = 4,
    roundId = 2,
    player = bob,
    hand = Hand(), // ainda não jogou
    rollCount = 0,
    state = TurnState.IN_PROGRESS,
    score = null
)

val roundNoPlay = Round(
    id = 1,
    matchId = 1,
    roundNumber = 1,
    turns = mutableListOf(aliceTurn1NoRoll, bobTurn1),
    currentPlayer = alice
)

val round = Round(
    id = 1,
    matchId = 1,
    roundNumber = 1,
    turns = mutableListOf(aliceTurn1, bobTurn1),
    currentPlayer = alice
)

val round2 = Round(
    id = 2,
    matchId = 1,
    roundNumber = 2,
    turns = mutableListOf(aliceTurn2, bobTurn2),
    currentPlayer = bob
)

val waitingRound = Round(
    id = 1,
    matchId = 1,
    roundNumber = 1,
    turns = mutableListOf(aliceTurn1, bobTurnWaiting),
    currentPlayer = bob
)

val roundWithWinner = Round(
    id = 1,
    matchId = 1,
    roundNumber = 1,
    turns = mutableListOf(aliceWinner, bobLoser),
    currentPlayer = alice,
    winners = mutableListOf(alice)
)

val finalRound = Round(
    id = 1,
    matchId = 1,
    roundNumber = 1,
    turns = mutableListOf(aliceWinner, bobLoser),
    currentPlayer = alice,
    winners = mutableListOf(alice)
)

val matchNoPlay = Match(
    id = 1,
    lobbyId = 1,
    players = mutableListOf(alice, bob),
    rounds = mutableListOf(roundNoPlay, round2),
    currentRound = 1
)

val match = Match(
    id = 1,
    lobbyId = 1,
    players = mutableListOf(alice, bob),
    rounds = mutableListOf(round, round2),
    currentRound = 1
)

val matchWaiting = Match(
    id = 1,
    lobbyId = 1,
    players = mutableListOf(alice, bob),
    rounds = mutableListOf(waitingRound, round2),
    currentRound = 1
)

val matchWinnerInProgress = Match(
    id = 1,
    lobbyId = 1,
    players = mutableListOf(alice, bob),
    rounds = mutableListOf(roundWithWinner),
    currentRound = 1,
    state = MatchState.IN_PROGRESS
)

val completedMatch = Match(
    id = 1,
    lobbyId = 1,
    players = mutableListOf(alice, bob),
    rounds = mutableListOf(finalRound),
    currentRound = 1,
    state = MatchState.COMPLETED,
    winners = mutableListOf(alice)
)