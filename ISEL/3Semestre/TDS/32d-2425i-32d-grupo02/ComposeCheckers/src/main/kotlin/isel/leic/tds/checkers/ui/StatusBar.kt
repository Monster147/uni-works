package isel.leic.tds.checkers.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.leic.tds.checkers.model.*
import isel.leic.tds.checkers.model.Player.BLACK
import isel.leic.tds.checkers.model.Player.WHITE

val STATUS_BAR_FONT_SIZE = when (BOARD_DIM) {
    8 -> 32.sp
    4 -> 24.sp
    else -> 28.sp
}

val STATUS_BAR_IMAGE_SIZE = when (BOARD_DIM) {
    8 -> 32.dp
    4 -> 24.dp
    else -> 28.dp
}

@Composable
fun StatusBar(board: Board?, you: Player?, clashName: String?) {
    Row(
        modifier = Modifier.width(GRID_SIDE).background(color3).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            clashName?.let {
                Text("Clash: $it", fontSize = STATUS_BAR_FONT_SIZE, color = Color.White)
            }
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                you?.let {
                    Text("You: ", fontSize = STATUS_BAR_FONT_SIZE, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Player(it, modifier = Modifier.size(STATUS_BAR_IMAGE_SIZE))
                }
            }
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            val (state, player) = when (board) {
                is BoardPlaying -> "Turn: " to board.currPlayer
                is WinnerBoard -> "Winner: " to board.winner
                is DrawBoard -> "Draw" to null
                null -> "No board" to null
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(state, fontSize = STATUS_BAR_FONT_SIZE, color = Color.White)
                player?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    Player(it, modifier = Modifier.size(STATUS_BAR_IMAGE_SIZE))
                }
            }
        }
    }
}

@Composable
@Preview
fun StatusBarTest() {
    val pieces = mapOf(
        Square(Row(6), Column(1)) to Piece(WHITE, Square(Row(6), Column(1))),
        Square(Row(5), Column(2)) to Piece(WHITE, Square(Row(5), Column(2)))
    )
    StatusBar(WinnerBoard(pieces,Player.WHITE, 0), Player.WHITE, "game")
}