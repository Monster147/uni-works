package isel.leic.tds.checkers.model

import isel.leic.tds.storage.Serializer

typealias Score = Map<Player?,Int>

data class Game(
    val board: Board? = null,
    val firstPlayer: Player = Player.entries.first(),
    val score: Score = (Player.entries+null).associateWith { 0 }
)

// Format: <firstPlayer> # <score> # <board>
// Example: "BLACK # WHITE:2 BLACK:1 null:4 # RUN BLACK 0 | WHITE/2b/false BLACK/3c/false"
object GameSerializer: Serializer<Game> {
    override fun serialize(data: Game): String {
        val score = data.score.map { "${it.key}:${it.value}" }.joinToString(" ")
        val board = data.board?.let { BoardSerializer.serialize(it) } ?: ""
        return "${data.firstPlayer} # $score # $board"
    }
    override fun deserialize(text: String): Game {
        val (player, score, board) = text.split(" # ")
        val firstPlayer = Player.valueOf(player)
        val scoreMap = score.split(" ").associate {
            val (ply, cnt) = it.split(":")
            Player.entries.firstOrNull { it.name==ply } to cnt.toInt()
        }
        val boardData = if(board.isNotEmpty()) BoardSerializer.deserialize(board) else null
        return Game(boardData, firstPlayer, scoreMap)
    }
}

// Função para adicionar mais um ao jogador que ganhar um jogo
private fun Score.advance(player: Player?) =
    this - player + (player to checkNotNull(this[player]) +1)


// Função para criar um novo tabuleiro
fun Game.newBoard() = Game(
    board = Board(player = firstPlayer),
    firstPlayer = firstPlayer.other,
    score = if (board is BoardPlaying) score.advance(board.currPlayer.other) else score
)

// Função para jogar um jogo
fun Game.play(from: Square, to: Square): Game {
    checkNotNull(board) { "No board" }
    val board = board.play(from, to)
    return copy(
        board = board,
        score = when(board) {
            is WinnerBoard -> score.advance(board.winner)
            is DrawBoard -> score.advance(null)
            is BoardPlaying -> score
        }
    )
}




