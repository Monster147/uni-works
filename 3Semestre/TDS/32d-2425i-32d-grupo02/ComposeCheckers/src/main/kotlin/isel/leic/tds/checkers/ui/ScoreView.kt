package isel.leic.tds.checkers.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.leic.tds.checkers.model.Player
import isel.leic.tds.checkers.model.Score

@Composable
@Preview
fun ScoreViewPreview() = ScoreViewContent(
    mapOf(Player.BLACK to 0, Player.WHITE to 1, null to 0)
)

@Composable
private fun ScoreViewContent(score: Score) = Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth()
) {
    Column {
        Player.entries.forEach {
            Row {
                Player(it, modifier = Modifier.size(32.dp))
                Text(" - ${score[it]}", style = MaterialTheme.typography.h4)
            }
        }
    }
    Text("Draw - ${score[null]}", style = MaterialTheme.typography.h4)
}

@Composable
fun ScoreView(score: Score, onClose: ()->Unit) = AlertDialog(
    onDismissRequest = onClose,
    confirmButton = { TextButton(onClick = onClose) {Text("Close")} },
    text = { ScoreViewContent(score) },
    title = { Text("Score", style = MaterialTheme.typography.h3) }
)