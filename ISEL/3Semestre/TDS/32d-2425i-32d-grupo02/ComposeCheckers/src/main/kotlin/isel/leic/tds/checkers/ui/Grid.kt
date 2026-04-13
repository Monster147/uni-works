package isel.leic.tds.checkers.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

import isel.leic.tds.checkers.model.*

val CELL_SIDE = 75.dp
val LINE_WIDTH = 10.dp
val FONT_SIZE = 28.sp
val GRID_SIDE = CELL_SIDE * BOARD_DIM + LINE_WIDTH * (BOARD_DIM-1)
private val lineColor = Color.Black // Color for the grid lines
private val lineThickness = 1.dp   // Thickness of the grid lines


/*
private val color2 = Color(247, 183, 110)*/

private val color1 = Color(134,87,62,255)
private val color2 = Color(238,202,144,255)
val color3 = Color(54, 25, 0)
private val color4 = Color.Red
private val targetColor = Color.Green

@Composable
fun Grid(board: Board?, sidePlayer: Player?, onClickCell: (Square)->Unit, selectedSquare: Square?, targets: List<Square> = emptyList()) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.border(lineThickness, Color.White)
    ) {
        repeat(BOARD_DIM) { row ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(BOARD_DIM) { col ->
                    val pos = if (sidePlayer != Player.BLACK) {
                        Square(Row(row), Column(col))  // Brancas vêem as peças normais
                    } else {
                        Square(Row(BOARD_DIM - row - 1), Column(col)) // Pretas vêem o tabuleiro invertido
                    }
                    val cellColor = if (pos.black) color1 else color2
                    val piece = board?.places?.get(pos)
                    val borderColor = if (pos == selectedSquare && piece != null) color4 else lineColor

                    Box(
                        modifier = Modifier
                            .size(CELL_SIDE)
                            .background(cellColor)
                            .border(lineThickness, borderColor) // Border around each cell
                            .clickable{
                                if (piece?.player == sidePlayer && piece != null) { // Verifica se a peça é do jogador White
                                    onClickCell(pos)
                                }},
                        contentAlignment = Alignment.Center
                    ){
                        if (pos in targets) {
                            Canvas(modifier = Modifier.size(CELL_SIDE)) {
                                drawCircle(
                                    color = targetColor,
                                    radius = size.minDimension / 3,
                                    center = center
                                )
                            }
                        }
                    Player(
                        player = piece?.player,
                        isQueen = piece?.isQueen == true,
                        Modifier.size(CELL_SIDE),
                        onClick = { onClickCell(pos) }
                    )}
                }
            }
        }
    }
}

@Composable
fun BoardWithLabels(board: Board?, sidePlayer: Player?, showTargets: Boolean,onClickCell: (Square, Square) -> Unit) {
    var firstClick: Square? by remember { mutableStateOf(null) }
    var selectedSquare: Square? by remember { mutableStateOf(null) }
    var targets: List<Square> by remember { mutableStateOf(emptyList()) }

    Row {
        // Coluna com os rótulos das linhas (8, 7, 6, ...)
        Column (modifier = Modifier.background(color3)){
            // Espaço vazio para alinhar com os rótulos das colunas
            Box(modifier = Modifier.size(CELL_SIDE))
            // Rótulos das linhas
            val rowLabels = if (sidePlayer != Player.BLACK) (BOARD_DIM downTo 1) else (1..BOARD_DIM)
            for (rowIdx in rowLabels) {
                Box(
                    modifier = Modifier
                        .size(CELL_SIDE)
                        .wrapContentSize(Alignment.CenterEnd)
                ) {
                    Text(text = rowIdx.toString(), color = Color.White, fontSize = FONT_SIZE)
                }
            }
        }
        Column {
            // Linha com os rótulos das colunas (a, b, c, ...)
            Row(
                horizontalArrangement = Arrangement.Start, // Alinhar os elementos a partir do início
                modifier = Modifier.background(color3)// Garantir que a largura corresponde à do tabuleiro
            )  {
                // Rótulos das colunas
                repeat(BOARD_DIM) { colIdx ->
                    Box(
                        modifier = Modifier
                            .size(CELL_SIDE)
                            .wrapContentSize(Alignment.BottomCenter)
                    ) {
                        Text(text = ('a' + colIdx).toString(), color = Color.White, fontSize = FONT_SIZE)
                    }
                    // Adicionar a coluna extra à direita com o mesmo fundo

                }
                Box(modifier = Modifier.size(CELL_SIDE).background(color3))
            }

            // The main grid (chessboard)
            Row {
                Grid(board = board, sidePlayer = sidePlayer, onClickCell = { clickedPos ->
                    // Se o primeiro clique já foi feito, define o segundo clique como destino
                    if (selectedSquare == clickedPos) {
                        selectedSquare = null // Desmarca o quadrado selecionado
                        firstClick = null // Reseta a origem também
                        targets = emptyList()
                        return@Grid // Ignora o restante da lógica
                    } else {
                        if (firstClick == null) {
                            firstClick = clickedPos // Marca o quadrado de origem
                            selectedSquare = clickedPos // Mostra a borda no quadrado selecionado
                            if (showTargets) {
                                targets = board?.calculatePossibleMoves(clickedPos, board.places) ?: emptyList()
                            }
                        } else {
                            firstClick?.let { from ->
                                val to = clickedPos
                                onClickCell(from, to) // Realiza o movimento
                                firstClick = null // Reseta para o próximo clique
                                selectedSquare = null // Remove a borda
                                targets = emptyList()
                            }
                        }
                    }
                },
                    selectedSquare = selectedSquare, targets)

                // Coluna extra à direita para pintar o lado direito
                Column(modifier = Modifier.background(color3)) {
                    repeat(BOARD_DIM - 1) {
                        Box(modifier = Modifier.size(CELL_SIDE))
                    }
                    // Caixa de preenchimento do canto inferior direito
                    Box(modifier = Modifier.size(CELL_SIDE).background(color3))
                }
            }
        }
    }
}


@Composable
@Preview
fun GridPreview() {
    val from = Square(Row(5), Column(0))
    val to = Square(Row(4), Column(1))
    val board = Board().play(from, to)
    Grid(board, Player.WHITE, {},null)
}

@Composable
@Preview
fun BoardWithLabelsPreview() {
    val from = Square(Row(5), Column(0))
    val to = Square(Row(4), Column(1))
    val board = Board().play(from, to)
    BoardWithLabels(board, Player.WHITE, false) { squareFrom, squareTo ->
        println("Moving from: $squareFrom to: $squareTo")}
}