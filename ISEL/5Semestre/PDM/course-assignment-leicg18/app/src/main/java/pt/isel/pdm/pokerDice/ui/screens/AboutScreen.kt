package pt.isel.pdm.pokerDice.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.ui.TopBar

const val RULES_BUTTON_TAG = "RulesButton"
const val GMAIL_BUTTON_TAG = "GmailButton"
const val DEVELOPERS_TEXT_TAG = "DevelopersText"

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onNavigateToTitle: () -> Unit = {},
    onRulesClick: () -> Unit = { },
    onGmailClick: () -> Unit = { },
) {
    TopBar(
        onBackIntent = onNavigateToTitle
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Poker Dice is a simple dice game where 5 dice marked with card values " +
                    "(9, 10, J, Q, K, A) are rolled to form poker-style hands. " +
                    "Players may re-roll dice up to three times to aim for the best hand, " +
                    "such as Five of a Kind, Full House, or a Straight. " +
                    "The winner is the player with the highest-ranking combination.\nFor more details information click on rules.",
            textAlign = TextAlign.Center
        )
        Button(onClick = { onRulesClick() }, modifier = Modifier.testTag(RULES_BUTTON_TAG)) {
            Text(text = stringResource(id = R.string.rules))
        }
        FilledIconButton(
            onClick = { onGmailClick() },
            modifier = Modifier.testTag(GMAIL_BUTTON_TAG)
        ) {
            Icon(Icons.Default.Email, contentDescription = "Gmail")
        }

        Text(
            text = stringResource(id = R.string.devolped_by) + ":\nJosé Saldanha, nº51445;\nRicardo Pinto, nº51447;\nVasco Piloto, nº 51484",
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(DEVELOPERS_TEXT_TAG),
        )
    }
}


@Preview
@Composable
fun AboutScreenPreview() {
    AboutScreen(onNavigateToTitle = {}, onRulesClick = {}, onGmailClick = {}, modifier = Modifier)
}