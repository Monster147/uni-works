import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import isel.leic.tds.checkers.model.Player
import isel.leic.tds.checkers.ui.Player

@Composable
@Preview
private fun PlayerTestApp() {
    var player by remember { mutableStateOf(Player.BLACK) }

    MaterialTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Player(player)
            Button(onClick = {
                player = player.other
            }) {
                Text("Change Player")
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Checkers",
        state = rememberWindowState(size = DpSize.Unspecified)
    ) {
        PlayerTestApp()
    }
}