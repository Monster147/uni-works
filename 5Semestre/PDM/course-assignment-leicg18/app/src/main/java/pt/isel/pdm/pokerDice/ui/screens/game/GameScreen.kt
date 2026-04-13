package pt.isel.pdm.pokerDice.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import pt.isel.pdm.pokerDice.R
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
import pt.isel.pdm.pokerDice.ui.TopBar
import pt.isel.pdm.pokerDice.ui.screens.matchViewModelPreview
import pt.isel.pdm.pokerDice.viewModels.HostState
import pt.isel.pdm.pokerDice.viewModels.MatchViewModel

const val LAZY_COLUMN_MATCH_TAG = "LazyColumnMatch"
const val BACK_TO_LOBBIES_TAG = "BackToLobbiesButton"
const val WAITING_FOR_TURN_TAG = "WaitingForTurnText"

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    matchId: Int,
    viewModel: MatchViewModel,
    matchPreview: Match? = null,
    currRound: Round? = null,
    currUserIdPreview: Int? = null,
    onNavigateToLobbies: () -> Unit = {}
) {
    val matchState = viewModel.currentMatch.collectAsState()
    val hostState = viewModel.hostState.collectAsState().value

    LaunchedEffect(matchId) {
        viewModel.fetchMatch(matchId)
    }

    val match = matchState.value

    if (match == null && matchPreview == null) {
        GameLoadingScreen()
    } else {
        val effectiveMatch = requireNotNull(match ?: matchPreview)

        when (effectiveMatch.state) {

            MatchState.IN_PROGRESS -> {
                GameContentScreen(
                    match = effectiveMatch,
                    viewModel = viewModel,
                    modifier = modifier,
                    currRoundPreview = currRound,
                    currUserIdPreview = currUserIdPreview,
                    hostState = hostState
                )
            }

            MatchState.COMPLETED -> MatchResultsScreen(
                modifier = modifier,
                match = effectiveMatch,
                onNavigateToLobbies = onNavigateToLobbies
            )
        }
    }
}

@Composable
fun GameContentScreen(
    match: Match,
    viewModel: MatchViewModel,
    modifier: Modifier = Modifier,
    currRoundPreview: Round? = null,
    currUserIdPreview: Int? = null,
    hostState: HostState,
) {
    val currRoundState = viewModel.currentRound.collectAsState()
    val currRound = currRoundState.value ?: currRoundPreview
    val sseState = viewModel.sseState.collectAsState().value

    LaunchedEffect(match.id) {
        viewModel.startListeningToMatchEvents(match.id)
    }

    if (match.currentRound == 0) {
        when (hostState) {
            HostState.Host -> {
                HostWaitingScreen(
                    sseState = sseState,
                    onStartFirstRound = { viewModel.startNextRound(match.id) }
                )
            }

            HostState.NotHost -> {
                RoundLoadingScreen(sseState = sseState)
            }

            HostState.Loading -> {
                RoundLoadingScreen(sseState = sseState)
            }
        }
        return
    }

    val currentUserId = viewModel.currUserLoggedIn?.id ?: currUserIdPreview


    if (currRound == null || currentUserId == null) {
        RoundLoadingScreen(sseState = sseState)
        return
    }

    val players = match.players
    val currentRoundNumber = match.currentRound
    val currRoundPlayer = currRound.currentPlayer
    val currentRoundPlayerId = currRoundPlayer.id

    val myTurn = currRound.turns.find { it.player.id == currentUserId }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.round) + ": ${if (match.currentRound <= match.rounds.size) currentRoundNumber else match.rounds.size}/${match.rounds.size}",
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag(LAZY_COLUMN_MATCH_TAG),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = rememberLazyListState()
        ) {
            items(items = players) { player ->
                val turn = currRound.turns.find { it.player.id == player.id }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (player.id == currentRoundPlayerId && currRound.winners.isEmpty()) {
                        Text(
                            text = "${player.name} (" + stringResource(R.string.playing) + ") 🎲",
                            color = Color.Red,
                            modifier = Modifier.testTag("PLAYER_${player.id}_STATUS")
                        )
                    } else if (player.id in currRound.winners.map { it.id }) {
                        Text(
                            text = "${player.name} (" + stringResource(R.string.winner) + ") 🏆",
                            color = Color(0xFFFFD700),
                            modifier = Modifier.testTag("PLAYER_${player.id}_STATUS")
                        )
                    } else {
                        Text(
                            text = player.name,
                            modifier = Modifier.testTag("PLAYER_${player.id}_STATUS")
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = DividerDefaults.Thickness,
                        color = Color.Gray
                    )
                    Text(
                        text = turn?.handSummary() ?: stringResource(R.string.no_play),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("PLAYER_${player.id}_HAND")
                    )
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "9=NINE, T=TEN, J=JACK, Q=QUEEN, K=KING, A=ACE",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when {
                        myTurn != null && currentUserId != currentRoundPlayerId -> {
                            Column (
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ){
                                if(myTurn.hand.dice.isNotEmpty()) {
                                    RenderHandSummary(
                                        turn = myTurn,
                                        modifier = Modifier.padding(16.dp)
                                    )

                                }

                                Text(
                                    text = stringResource(R.string.waiting_for) + " ${currRoundPlayer.name} " + stringResource(
                                        R.string.to_play
                                    ) + "...",
                                    modifier = Modifier.padding(16.dp).testTag(WAITING_FOR_TURN_TAG),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        myTurn == null -> CircularProgressIndicator(
                            Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )

                        else ->
                            MatchSide(
                                modifier = Modifier
                                    .navigationBarsPadding(),
                                onReroll = { keptDice ->
                                    if (keptDice != null) viewModel.playTurn(
                                        match.id,
                                        currRoundPlayer,
                                        keptDice
                                    )
                                    else viewModel.playTurn(match.id, currRoundPlayer)
                                },
                                onSkip = {
                                    viewModel.passTurn(match.id, currRoundPlayer)
                                },
                                turn = myTurn,
                            )
                    }
                }
            }
        }
    }

}


fun Turn.handSummary(): String {
    if (hand.dice.isEmpty()) return "No play yet"
    val diceStr = hand.dice.joinToString("") { dice ->
        when (dice.face) {
            DiceFace.ACE -> "A"
            DiceFace.KING -> "K"
            DiceFace.QUEEN -> "Q"
            DiceFace.JACK -> "J"
            DiceFace.TEN -> "T"
            DiceFace.NINE -> "9"
        }
    }
    val scoreStr = score?.toScoreString() ?: "Score: N/A"
    return "$diceStr  [$scoreStr]"
}

fun HandCategory.toScoreString(): String {
    return when (this) {
        HandCategory.HIGH_CARD -> "High Card"
        HandCategory.ONE_PAIR -> "Pair"
        HandCategory.TWO_PAIR -> "Two Pair"
        HandCategory.THREE_OF_A_KIND -> "Three of a Kind"
        HandCategory.STRAIGHT -> "Straight"
        HandCategory.FULL_HOUSE -> "Full House"
        HandCategory.FOUR_OF_A_KIND -> "Four of a Kind"
        HandCategory.FIVE_OF_A_KIND -> "Five of a Kind"
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenLoadingPreview() {
    GameScreen(
        matchId = 1,
        viewModel = matchViewModelPreview
    )
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {


    val bobTurn = Turn(
        id = 2,
        roundId = 1,
        player = bob,
        hand = Hand(), // ainda não jogou
        rollCount = 0,
        state = TurnState.IN_PROGRESS,
        score = null
    )

    val round = Round(
        id = 1,
        matchId = 1,
        roundNumber = 1,
        turns = mutableListOf(aliceTurn, bobTurn),
        currentPlayer = alice
    )

    val match = Match(
        id = 1,
        lobbyId = 1,
        players = mutableListOf(alice, bob),
        rounds = mutableListOf(round),
        currentRound = 1
    )

    val previewRound = match.rounds.firstOrNull()
    GameScreen(
        matchId = 1,
        viewModel = matchViewModelPreview,
        matchPreview = match,
        currRound = previewRound,
        currUserIdPreview = alice.id
    )
}

@Preview(showBackground = true)
@Composable
fun GameScreenWaitingPreview() {

    val bobTurn = Turn(
        id = 2,
        roundId = 1,
        player = bob,
        hand = Hand(
            dice = listOf(DiceFace.NINE, DiceFace.TEN, DiceFace.JACK, DiceFace.QUEEN, DiceFace.KING).map { Dice(it) }
        ),
        rollCount = 1,
        state = TurnState.IN_PROGRESS,
        score = null
    )

    val round = Round(
        id = 1,
        matchId = 1,
        roundNumber = 1,
        turns = mutableListOf(aliceTurn, bobTurn),
        currentPlayer = bob // Bob está a jogar
    )

    val match = Match(
        id = 1,
        lobbyId = 1,
        players = mutableListOf(alice, bob),
        rounds = mutableListOf(round),
        currentRound = 1
    )

    GameScreen(
        matchId = match.id,
        viewModel = matchViewModelPreview,
        matchPreview = match,
        currRound = round,
        currUserIdPreview = alice.id
    )
}

@Preview(showBackground = true)
@Composable
fun GameScreenRoundWinnerPreview() {
    val aliceWinningTurn = Turn(
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
            ).map { Dice(it) }
        ),
        rollCount = 2,
        state = TurnState.COMPLETED,
        score = HandCategory.STRAIGHT
    )
    val bobTurn = Turn(
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
        rollCount = 2,
        state = TurnState.COMPLETED,
        score = HandCategory.HIGH_CARD
    )
    val roundWithWinner = Round(
        id = 1,
        matchId = 1,
        roundNumber = 1,
        turns = mutableListOf(aliceWinningTurn, bobTurn),
        currentPlayer = alice,
        winners = mutableListOf(alice)
    )
    val matchInProgress = Match(
        id = 1,
        lobbyId = 1,
        players = mutableListOf(alice, bob),
        rounds = mutableListOf(roundWithWinner),
        currentRound = 1,
        state = MatchState.IN_PROGRESS
    )
    GameScreen(
        matchId = matchInProgress.id,
        viewModel = matchViewModelPreview,
        matchPreview = matchInProgress,
        currRound = roundWithWinner,
        currUserIdPreview = alice.id
    )
}


@Preview(showBackground = true)
@Composable
fun GameScreenGameWinnerPreview() {
    val aliceWinningTurn = Turn(
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
            ).map { Dice(it) }
        ),
        rollCount = 2,
        state = TurnState.COMPLETED,
        score = HandCategory.STRAIGHT
    )
    val bobTurn = Turn(
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
        rollCount = 2,
        state = TurnState.COMPLETED,
        score = HandCategory.HIGH_CARD
    )

    val finalRound = Round(
        id = 1,
        matchId = 1,
        roundNumber = 1,
        turns = mutableListOf(aliceWinningTurn, bobTurn),
        currentPlayer = alice,
        winners = mutableListOf(alice)
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

    GameScreen(
        matchId = completedMatch.id,
        viewModel = matchViewModelPreview,
        matchPreview = completedMatch,
        currRound = finalRound,
        currUserIdPreview = alice.id
    )
}
