package pt.isel.pdm.pokerDice.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

const val LOBBY_BUTTON_TAG = "LobbyButton"

@Composable
fun TitleScreen(
    onNavigateToAbout: () -> Unit,
    onNavigateToLobbies: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopBar(
        onInfoIntent = onNavigateToAbout,
        onProfileIntent = onNavigateToProfile
    )
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Poker Dice",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Button(onClick = onNavigateToLobbies, modifier = Modifier.testTag(LOBBY_BUTTON_TAG)) {
            Text(text = stringResource(id = R.string.lobbies))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TitleScreenPreview() {
    TitleScreen(onNavigateToAbout = {}, onNavigateToLobbies = {}, onNavigateToProfile = {})
}
