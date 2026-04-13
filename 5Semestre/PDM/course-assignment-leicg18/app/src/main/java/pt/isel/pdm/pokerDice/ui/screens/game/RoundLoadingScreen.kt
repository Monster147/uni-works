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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.viewModels.SseConnectionState

@Composable
fun RoundLoadingScreen(
    sseState: SseConnectionState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.height(8.dp))

            val message = when (sseState) {
                SseConnectionState.CONNECTING ->
                    stringResource(R.string.sse_connecting)

                SseConnectionState.CONNECTED ->
                    stringResource(R.string.sse_connected_waiting)

                SseConnectionState.ERROR ->
                    stringResource(R.string.sse_error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = message)
        }
    }
}
