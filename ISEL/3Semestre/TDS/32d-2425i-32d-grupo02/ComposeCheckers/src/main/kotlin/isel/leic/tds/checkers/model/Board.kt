package isel.leic.tds.checkers.model

import isel.leic.tds.storage.*
import kotlin.math.*

const val BOARD_DIM = 6
const val DRAW_TURNS = (BOARD_DIM/2)*5

sealed class Board(val places: Map<Square, Piece>){
    override fun equals(other: Any?) = other is Board && places == other.places
    override fun hashCode() = places.hashCode()

    companion object {
        // Função para criar o tabuleiro inicial
        // Retorna um mapa com uma peça associada a um quadrado
        fun InitialBoard():Map<Square, Piece> =
            buildMap {
                for (square in Square.values) {
                    val place = when {
                        square.row.index in 0..<(BOARD_DIM / 2) - 1 && square.black ->
                            Piece(Player.BLACK, Square(Row(square.row.index), Column(square.column.index)))
                        square.row.index in (BOARD_DIM / 2) + 1..<BOARD_DIM && square.black ->
                            Piece(Player.WHITE, Square(Row(square.row.index), Column(square.column.index)))
                        else -> null
                    }
                    if(place != null) put(square, place)
                }
            }
        val initialBoard = InitialBoard()
    }

    fun calculatePossibleMoves(from: Square, places: Map<Square, Piece>): List<Square> {
        // Obtém a peça na posição "from". Se não existir nenhuma peça, retorna uma lista vazia.
        val piece = places[from] ?: return emptyList()

        /*
         Define as direções possíveis de movimento para a peça.
         Se for uma rainha (queen), pode mover-se em todas as direções diagonais.
         Caso contrário, as direções dependem da cor da peça:
         - Peças pretas movem-se "para baixo" no tabuleiro.
         - Peças brancas movem-se "para cima" no tabuleiro.
        */
        val directions = if (piece.isQueen) {
            listOf(
                Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1) // Queen can move in all diagonal directions
            )
        } else {
            if (piece.player == Player.BLACK) {
                listOf(Pair(1, 1), Pair(1, -1)) // Black pieces move downwards
            } else {
                listOf(Pair(-1, 1), Pair(-1, -1)) // White pieces move upwards
            }
        }

        // Obtém as capturas possíveis para o jogador atual.
        val possibleCaptures = getPossibleCaptures(piece.player, places)

        // Se existirem capturas disponíveis, apenas estas são permitidas.
        if (possibleCaptures.isNotEmpty()) {
            // Lista de peças que podem capturar.
            val captorsList = possibleCaptures.map { it.captor }
            // Se a peça atual não estiver entre as que podem capturar, não tem movimentos válidos.
            if (piece !in captorsList) return emptyList()
            // Retorna apenas as capturas possíveis para a peça atual.
            return possibleCaptures.filter { it.captor == piece }.map { it.pos }
        }

        // Caso não existam capturas, calcula os movimentos normais para a peça.
        return directions.flatMap { (rowOffset, colOffset) ->
            generateMoves(from, rowOffset, colOffset, places, piece.isQueen)
        }
    }

    // Função auxiliar para gerar os movimentos válidos para uma direção específica.
    private fun generateMoves(from: Square, rowOffset: Int, colOffset: Int, places: Map<Square, Piece>, isQueen: Boolean): List<Square> {

        /*
         Função que calcula a próxima posição (linha e coluna) no tabuleiro com base na direção.
         Verifica também se a nova posição está dentro dos limites do tabuleiro.
        */
        fun nextPosition(row: Int, col: Int): Pair<Int, Int>? {
            val newRow = row + rowOffset
            val newCol = col + colOffset
            return if (newRow in 0 ..< BOARD_DIM && newCol in 0 ..< BOARD_DIM) Pair(newRow, newCol) else null
        }

        /*
         Função recursiva que gera todas as posições válidas para uma peça, numa direção específica.
         Para peças normais, devolve apenas uma posição (o próximo quadrado vazio).
         Para a rainha, continua a gerar posições até encontrar um obstáculo ou sair do tabuleiro.
        */
        fun generatePositions(row: Int, col: Int): List<Square> {
            val nextPos = nextPosition(row, col) // Calcula a próxima posição.
            return if (nextPos != null) {
                val (newRow, newCol) = nextPos
                val targetSquare = Square(Row(newRow), Column(newCol))
                // Se o quadrado de destino estiver vazio, adiciona-o à lista.
                if (places[targetSquare] == null) {
                    listOf(targetSquare) + if (isQueen) generatePositions(newRow, newCol) else emptyList()
                } else {
                    emptyList() // Se encontrar uma peça, para de gerar posições.
                }
            } else {
                emptyList() // Se sair do tabuleiro, retorna uma lista vazia.
            }
        }

        return generatePositions(from.row.index, from.column.index)
    }

}

class WinnerBoard(places: Map<Square, Piece>, val winner:Player, val movesWithoutCapture : Int) : Board(places)

class DrawBoard(places: Map<Square, Piece>) : Board(places)

class BoardPlaying(
    places: Map<Square, Piece> = initialBoard,
    val currPlayer: Player,
    val movesWithoutCapture : Int = 0) : Board(places){

    // Função de atualização do tabuleiro
    fun updateBoard(piece: Piece, to: Square, remove: Piece? = null): Board {
        val toQueen = piece.canBeQueen(to.row) || piece.isQueen
        val updatedPiece = Piece(piece.player, to, toQueen)

        // O novo map, é igual ao mapa já existente, mas retiramos a peça que mexemos e
        // adicionamos a peça com a nova posição
        val map: Map<Square, Piece> = places.minus(piece.pos).plus(to to updatedPiece)

        // Verifica se a peça a remover é null ou não
        return if(remove != null){
            val newMap = map - remove.pos
            when{
                // Caso o novo mapa fique só com peças brancas retorna um WinnerBoard com as peças brancas como vencedoras
                newMap.all{it.value.player == Player.WHITE} -> WinnerBoard(newMap, Player.WHITE, movesWithoutCapture)

                // Caso o novo mapa fique só com peças pretas retorna um WinnerBoard com as peças pretas como vencedoras
                newMap.all{it.value.player == Player.BLACK} -> WinnerBoard(newMap, Player.BLACK, movesWithoutCapture)

                // Caso o movesWithoutCapture fique igual ao DRAW_TURNS após o movimento retorna um DrawBoard
                movesWithoutCapture + 1 == DRAW_TURNS -> DrawBoard(newMap)

                // Caso contrário, vai verificar se a lista de capturas possíveis está vazia
                else ->
                    // Se estiver vazia, continua com um BoardPlaying, mas passa a vez
                    if(getPossibleCaptures(currPlayer, newMap).isEmpty())
                        BoardPlaying(newMap, currPlayer.other, 0)

                    // Caso contrário continua a vez do jogador atual
                    else
                        BoardPlaying(newMap, currPlayer, 0)
            }
        }
        // Caso não seja null
        else{
            when{
                map.all{it.value.player == Player.WHITE} -> WinnerBoard(map, Player.WHITE, movesWithoutCapture)
                map.all{it.value.player == Player.BLACK} -> WinnerBoard(map, Player.BLACK, movesWithoutCapture)
                // Verifica se o movesWithoutCapture fique igual ao DRAW_TURNS após o movimento retorna um DrawBoard
                movesWithoutCapture +1 == DRAW_TURNS -> DrawBoard(map)
                // Se não, retorna um BoardPlaying
                else -> BoardPlaying(map, currPlayer.other, movesWithoutCapture+1)
            }

        }
    }
}

object BoardSerializer: Serializer<Board> {
    // Converte um tabuleiro para uma linha de texto
    override fun serialize(data: Board): String {
        val moves = data.places.map{ "${it.value.player}/${it.value.pos}/${it.value.isQueen}" }.joinToString(" ")
        return when(data) {
            is BoardPlaying -> "RUN ${data.currPlayer} ${data.movesWithoutCapture}"
            is WinnerBoard -> "WIN ${data.winner} ${data.movesWithoutCapture}"
            is DrawBoard -> "DRAW null null"
        } + " | $moves"
    }

    // Esta função converte uma linha de texto, para um tabuleiro
    override fun deserialize(text: String): Board {
        val (state, plays) = text.split(" | ")
        val (type, player, turnsWithoutCapture) = state.split(" ")
        val moves : Map<Square, Piece> = if (plays.isEmpty()) emptyMap()
        else plays.split(" ").associate {
            val (ply, pos, isQueen) = it.split("/", " ")
            val sqr = pos.toSquare()
            val piece = Piece(Player.valueOf(ply), sqr, isQueen.toBoolean())
            sqr to piece
        }
        return when(type) {
            "RUN" -> BoardPlaying(moves, Player.valueOf(player), turnsWithoutCapture.toInt())
            "WIN" -> WinnerBoard(moves, Player.valueOf(player), turnsWithoutCapture.toInt())
            "DRAW" -> DrawBoard(moves)
            else -> error("Invalid state $type")
        }
    }
}

fun Board.play(from: Square, to: Square): Board{
    when(this) {
        is WinnerBoard -> error("Game Over: The player $winner already won this game")
        is DrawBoard -> error("Game Over: The game already ended in a draw")
        is BoardPlaying -> {
            require(to.black) { "You can't place a piece in a white square" }
            require(places.values.find { it.pos == to } == null) { "Square '$to' is occupied" }

            val fromPiece = places.values.find { it.pos == from }
            requireNotNull(fromPiece) { "Piece $fromPiece is not found" }
            require(fromPiece.player == currPlayer) { "You can only move your pieces" }

            val possibleMoves = calculatePossibleMoves(from, places)
            require(to in possibleMoves) { "Invalid move" }

            val possibleCaptures = getPossibleCaptures(currPlayer, places)
            val fromPieceCaptures = possibleCaptures.filter { it.captor == fromPiece }
            val remove = fromPieceCaptures.firstOrNull { it.pos == to }?.captured

            val newBoard = updateBoard(fromPiece, to, remove)
            println("$newBoard")
            val newPossibleCaptures = getPossibleCaptures(currPlayer, newBoard.places)
            return if (newPossibleCaptures.isNotEmpty() && newPossibleCaptures.any { it.captor?.pos == to }) {
                newBoard
            } else {
                newBoard.switchTurn(currPlayer.other, if (remove != null) 0 else movesWithoutCapture)
            }
        }
    }
}

fun Board.switchTurn(player: Player, movesWithoutCapture: Int): BoardPlaying {
    return BoardPlaying(places, player, movesWithoutCapture + 1)
}

fun Board(player: Player = Player.WHITE): Board = BoardPlaying(currPlayer = player)