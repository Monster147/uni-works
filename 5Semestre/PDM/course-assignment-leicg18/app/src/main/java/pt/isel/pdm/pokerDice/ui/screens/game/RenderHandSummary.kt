package pt.isel.pdm.pokerDice.ui.screens.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.Hand
import pt.isel.pdm.pokerDice.domain.Turn

@Composable
fun RenderHandSummary(
    modifier:Modifier = Modifier,
    turn: Turn,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            repeat(3) { index ->
                val dice = turn.hand.dice[index]
                Image(
                    painter = painterResource(id = getDiceRes(dice.face)),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(100.dp)
                )
            }
        }
        Row {
            repeat(2) { index ->
                val actualIndex = index + 3
                val dice = turn.hand.dice[actualIndex]
                Image(
                    painter = painterResource(id = getDiceRes(dice.face)),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(100.dp)
                )
            }
        }

        turn.score?.let {
            Text(
                text = stringResource(R.string.score) + ": ${it.toScoreString()}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}