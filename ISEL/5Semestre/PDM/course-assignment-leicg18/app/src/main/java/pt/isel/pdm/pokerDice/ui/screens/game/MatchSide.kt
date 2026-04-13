package pt.isel.pdm.pokerDice.ui.screens.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.Dice
import pt.isel.pdm.pokerDice.domain.DiceFace
import pt.isel.pdm.pokerDice.domain.Hand
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Turn
import pt.isel.pdm.pokerDice.domain.TurnState
import pt.isel.pdm.pokerDice.domain.User

const val MAX_REROLL = 3
const val IMAGE_TAG = "Image tag"
const val REROLL_BUTTON_TAG = "Reroll Button"
const val SKIP_BUTTON_TAG = "Skip Button"
const val YOUR_TURN_TAG = "Your turn"
const val REROLL_TEXT_TAG = "Reroll Text"
const val REROLL_COUNT_TAG = "Reroll Count"
const val SCORE_TEXT_TAG = "Score Text"



@Composable
fun MatchSide(
    modifier: Modifier = Modifier,
    turn: Turn,
    onReroll: (keptDice: List<Boolean>?) -> Unit = { },
    onSkip: () -> Unit = { }
) {
    val selectedStates = remember { mutableStateListOf(false, false, false, false, false) }
    // SnapshotStateList por ser mais simples e sem necessidade de criar uma nova lista.
    //lista que representa a selecao dos dados

    var rerollCount by remember { mutableIntStateOf(turn.rollCount) }
    var firstRoll by remember { mutableStateOf(turn.hand.dice.isEmpty()) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (turn.hand.dice.isEmpty()) {
            Text(
                text = stringResource(R.string.turn_roll),
                modifier = Modifier.padding(16.dp).testTag(YOUR_TURN_TAG)
            )
        } else {
            Row {
                repeat(3) { index ->
                    val dice = turn.hand.dice[index]
                    SelectImage(
                        imageRes = getDiceRes(dice.face),
                        isSelected = selectedStates[index],
                        modifier = Modifier
                            .padding(4.dp)
                            .testTag(IMAGE_TAG),
                        onClick = {
                            selectedStates[index] = !selectedStates[index]
                        }
                    )
                }
            }
            Row {
                repeat(2) { index ->
                    val actualIndex = index + 3
                    val dice = turn.hand.dice[actualIndex]
                    SelectImage(
                        imageRes = getDiceRes(dice.face),
                        isSelected = selectedStates[actualIndex],
                        modifier = Modifier
                            .padding(4.dp)
                            .testTag(IMAGE_TAG),
                        onClick = {
                            selectedStates[actualIndex] = !selectedStates[actualIndex]
                        }
                    )
                }
            }
        }

        Spacer(modifier = modifier.height(16.dp))

        if (turn.score != null) {
            val scoreString = turn.score?.toScoreString() ?: "N/A"
            Text(
                text = stringResource(R.string.score) + ": $scoreString",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag(SCORE_TEXT_TAG),
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    rerollCount++
                    if (!firstRoll) onReroll(selectedStates.map { !it })
                    else onReroll(null)
                    firstRoll = false
                    selectedStates.replaceAll { false }
                },
                modifier = modifier
                    .padding(8.dp)
                    .testTag(REROLL_BUTTON_TAG),
                shape = CircleShape,
                enabled = firstRoll || ( rerollCount < MAX_REROLL && selectedStates.any { it } )
            ) {
                Text(
                    text = if (turn.hand.dice.isEmpty()) stringResource(R.string.roll) else stringResource(
                        R.string.reroll
                    ),
                    modifier = Modifier.testTag(REROLL_TEXT_TAG)
                )
            }
            Button(
                onClick = {
                    onSkip()
                },
                modifier = modifier
                    .padding(8.dp)
                    .testTag(SKIP_BUTTON_TAG),
                shape = CircleShape,
                enabled = !firstRoll
            ) {
                Text(text = stringResource(R.string.skip))
            }
        }

        Text(
            text = stringResource(R.string.reroll_count) + ": $rerollCount/$MAX_REROLL",
            modifier = modifier.fillMaxWidth().testTag(REROLL_COUNT_TAG),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SelectImage(
    modifier: Modifier = Modifier,
    imageRes: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.Red else Color.Gray,
                shape = RoundedCornerShape(24.dp)
            )
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun getDiceRes(face: DiceFace): Int {
    return when (face) {
        DiceFace.NINE -> R.drawable.dice_nine
        DiceFace.TEN -> R.drawable.dice_ten
        DiceFace.JACK -> R.drawable.dice_jack
        DiceFace.QUEEN -> R.drawable.dice_queen
        DiceFace.KING -> R.drawable.dice_king
        DiceFace.ACE -> R.drawable.dice_ace
    }
}

@Preview(showBackground = true)
@Composable
fun MatchSideWithHandPreview() {
    MatchSide(
        turn =
            Turn(
                id = 1,
                roundId = 1,
                player = User(
                    1000,
                    "Alice",
                    "alice@gmail.com",
                    PasswordValidationInfo("hash")
                ),
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
                score = null,
            )
    )
}

@Preview(showBackground = true)
@Composable
fun MatchSideWithoutHandPreview() {
    MatchSide(
        turn =
            Turn(
                id = 1,
                roundId = 1,
                player = User(
                    1000,
                    "Alice",
                    "alice@gmail.com",
                    PasswordValidationInfo("hash")
                ),
                hand = Hand(
                    dice = emptyList()
                ),
                rollCount = 1,
                state = TurnState.IN_PROGRESS,
                score = null,
            )
    )
}


@Preview(showBackground = true)
@Composable
fun SelectImagePreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SelectImage(imageRes = R.drawable.dice_ace, isSelected = false, onClick = {})
        SelectImage(imageRes = R.drawable.dice_jack, isSelected = true, onClick = {})
    }
}

