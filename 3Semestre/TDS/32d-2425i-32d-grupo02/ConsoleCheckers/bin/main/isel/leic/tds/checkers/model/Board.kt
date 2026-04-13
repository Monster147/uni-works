package isel.leic.tds.checkers.model

import isel.leic.tds.storage.*
import kotlin.math.*

const val BOARD_DIM = 8
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

            // Calcula a distância entre a coluna da próxima posição e a coluna da posição atual
            val colDis = from.column.index - to.column.index

            // Calcula a distância entre a linha da próxima posição e a linha da posição atual
            val rowDis = from.row.index - to.row.index

            require (abs(colDis) == abs(rowDis)) {"You need to move in diagonals"}

            // Obtem uma lista de possiveis capturas
            val possibleCaptures = getPossibleCaptures(currPlayer, places)

            // Verifica se a lista está vazia e se a peça a mover não é rainha, então verifica o movimento dos peões
            if (possibleCaptures.isEmpty()) {
                if (!fromPiece.isQueen) {
                    when (fromPiece.player) {

                        // Como as peças brancas estão na parte de baixo do tabuleiro, o rowDis tem de ser maior que 0
                        Player.WHITE -> require(rowDis > 0) {
                            "You can't move backwards with a pawn piece."
                        }

                        // Como as peças pretas estão na parte de cima do tabuleiro, o rowDis tem de ser menor que 0
                        Player.BLACK -> require(rowDis < 0) {
                            "You can't move backwards with a pawn piece."
                        }
                    }
                    require(abs(rowDis) == 1) { "You can only move one row if you don't have any possible capture." }
                }

                // Atualiza o tabuleiro
                return updateBoard(fromPiece, to)
            }

            // Caso exista captura possivel, atualizamos a lista para só termos as peças que podem capturar,
            // e verificamos se a peça que queremos mover está nessa mesma lista, caso esteja, obriga-se a capturar.
            val captorsList = possibleCaptures.map { it.captor }
            require(fromPiece in captorsList) {
                val capture = possibleCaptures.map { it.captured }.first()
                "There is a mandatory capture in ${capture?.pos}"
            }

            // Filtramos a lista das capturas possiveis de modo a termos só os elementos, ao qual,
            // em que quem captura é igual à peça a mexer, e de seguida verificamos se a próxima posição
            // está incluida na lista de posições após uma captura
            val fromPieceCaptures = possibleCaptures.filter { it.captor == fromPiece }
            val posAfterCapture = fromPieceCaptures.map { it.pos }
            require(to in posAfterCapture) {
                val capture = fromPieceCaptures.first().captured
                "There is a mandatory capture in ${capture?.pos}"
            }

            // Retiramos a peça que foi capturada da lista de capturas da peça a mexer
            // e atualizamos o tabuleiro, com o movimento da peça e a retirada da peça capturada
            val remove = fromPieceCaptures.first { it.pos == to }.captured
            return updateBoard(fromPiece, to, remove)
        }
    }
}

fun Board(player: Player = Player.WHITE): Board = BoardPlaying(currPlayer = player)