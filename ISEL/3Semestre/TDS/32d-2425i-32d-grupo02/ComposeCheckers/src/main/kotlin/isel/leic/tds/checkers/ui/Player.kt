package isel.leic.tds.checkers.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import isel.leic.tds.checkers.model.Player

@Composable
fun Player(
    player: Player?,
    isQueen: Boolean = false,
    modifier: Modifier = Modifier.size(100.dp),
    onClick: (()->Unit)? = null
) {
    if (player == null) {
        Box(
            modifier = onClick?.let { modifier.clickable(onClick = it) } ?: modifier
        )
    } else {
        val file = when {
            player == Player.BLACK && isQueen -> "piece_bk"
            player == Player.WHITE && isQueen -> "piece_wk"
            player == Player.BLACK -> "piece_b"
            player == Player.WHITE -> "piece_w"
            else -> null
        }
        file?.let {
        Image(
            painter = painterResource("$file.png"),
            contentDescription = file,
            modifier = modifier
        )
            }
    }
}

@Composable
@Preview
fun PlayerViewTest() {
    Row {
        Player(Player.BLACK)
        Player(Player.WHITE)
        Player(Player.BLACK, isQueen = true)
        Player(Player.WHITE, isQueen = true)
    }
}