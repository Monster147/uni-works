package pt.isel.pdm.pokerDice.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R

const val GAME_LOADING_TAG = "game_loading_tag"
@Composable
fun GameLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.game_loading),
                modifier = Modifier.testTag(GAME_LOADING_TAG)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameLoadingScreenPreview() {
    GameLoadingScreen()
}