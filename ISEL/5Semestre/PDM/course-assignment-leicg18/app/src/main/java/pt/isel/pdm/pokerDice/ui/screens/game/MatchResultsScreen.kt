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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.Match

@Composable
fun MatchResultsScreen(
    modifier: Modifier = Modifier,
    match: Match,
    onNavigateToLobbies: () -> Unit
) {
    val roundsWonCount = mutableMapOf<Int, Int>().apply {
        match.players.forEach { this[it.id] = 0 }
        match.rounds.forEach { round ->
            round.winners.forEach { winner ->
                this[winner.id] = (this[winner.id] ?: 0) + 1
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(match.rounds) { round ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.round_number, round.roundNumber),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.testTag("ROUND_${round.id}_NUMBER")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    round.turns.forEach { turn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = turn.player.name,
                                modifier = Modifier.testTag("ROUND_${round.id}_PLAYER_${turn.player.id}_NAME")
                            )
                            Text(
                                text = turn.handSummary(),
                                modifier = Modifier.testTag("ROUND_${round.id}_PLAYER_${turn.player.id}_HAND")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val winnerText = when {
                        round.winners.isEmpty() -> stringResource(R.string.no_winner)
                        round.winners.size == 1 -> stringResource(R.string.winner_of_round, round.winners.first().name)
                        else -> stringResource(R.string.tie) + ": " + round.winners.joinToString(", ") { it.name }
                    }

                    Text(
                        text = winnerText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ROUND_${round.id}_WINNER"),
                        textAlign = TextAlign.Center,
                        style =MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            val matchWinnerText = when {
                match.winners.isEmpty() -> stringResource(R.string.no_winner)
                match.winners.size == 1 -> {
                    val winner = match.winners.first()
                    val roundsWon = roundsWonCount[winner.id] ?: 0
                    "${winner.name} " + stringResource(R.string.won_match_with_rounds, roundsWon)
                }

                else -> stringResource(R.string.tie) + ": " + match.winners.joinToString(", ") { it.name }
            }

            Text(
                text = matchWinnerText,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("MATCH_WINNER"),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(BACK_TO_LOBBIES_TAG),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = onNavigateToLobbies) {
                    Text(
                        text = stringResource(R.string.back_lobbies),
                    )
                }
            }
        }
    }
}