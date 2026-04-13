package pt.isel.pdm.pokerDice.ui.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.viewModels.SseConnectionState

@Composable
fun HostWaitingScreen(
    sseState: SseConnectionState,
    onStartFirstRound: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (sseState) {
                SseConnectionState.CONNECTING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 4.dp,
                        color = colors.primary
                    )
                }

                SseConnectionState.CONNECTED -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.ready_to_start),
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(40.dp)
                    )
                }

                SseConnectionState.ERROR -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = stringResource(R.string.sse_error),
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (sseState) {
                    SseConnectionState.CONNECTING -> stringResource(R.string.waiting_for_connection)
                    SseConnectionState.CONNECTED -> stringResource(R.string.ready_to_start)
                    SseConnectionState.ERROR -> stringResource(R.string.sse_error)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartFirstRound,
                enabled = sseState == SseConnectionState.CONNECTED
            ) {
                Text(stringResource(R.string.start_first_round))
            }
        }
    }
}