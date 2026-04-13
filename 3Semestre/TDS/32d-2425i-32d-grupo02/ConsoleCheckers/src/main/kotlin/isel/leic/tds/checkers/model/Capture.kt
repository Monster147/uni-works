package isel.leic.tds.checkers.model

data class Capture(val captor: Piece?, val captured: Piece?, val pos: Square)

fun getPossibleCaptures(currPlayer: Player, places: Map<Square, Piece>): List<Capture> {
    // Filtra as peças aliadas e inimigas
    val allyPieces: List<Piece> = places.values.filter { it.player == currPlayer }
    val enemyPieces: List<Piece> = places.values.filter { it.player != currPlayer }

    // Cria a lista de capturas, usando o `canCapture`
    return allyPieces.flatMap { ally: Piece ->
        enemyPieces.flatMap { enemy: Piece ->
            if (ally.canCapture(enemy, places)) {
                val possiblePositions = getPossiblePosAfterCapture(ally, enemy, places)
                possiblePositions.map { (square: Square, _: Piece?) -> Capture(ally, enemy, square) }
            } else {
                emptyList()
            }
        }
    }
}

fun getPossiblePosAfterCapture(captor: Piece, captured: Piece, pieces: Map<Square, Piece>): Map<Square, Piece> {
    // Cálculo das direções de movimento baseadas na captura
    val rowDir = (captured.pos.row.index - captor.pos.row.index).coerceIn(-1, 1)
    val colDir = (captured.pos.column.index - captor.pos.column.index).coerceIn(-1, 1)

    // Função auxiliar recursiva para procurar posições possíveis para uma peça rainha
    fun exploreQueenPositions(
        rowOffset: Int, colOffset: Int, map: Map<Square, Piece> = emptyMap()
    ): Map<Square, Piece> {
        val newRowIdx = captured.pos.row.index + rowOffset
        val newColIdx = captured.pos.column.index + colOffset

        // Verifica se a posição está dentro dos limites do tabuleiro
        if (newRowIdx !in 0 ..< BOARD_DIM || newColIdx !in 0 ..< BOARD_DIM) return map

        val square = Square(Row(newRowIdx), Column(newColIdx))

        // Se a posição já está ocupada por uma peça, interrompe a procura
        if (pieces.containsKey(square)) return map

        val nextAcc = map + (square to captor)  // A peça captora ocupa a nova posição

        // Continua a procurar a próxima posição na direção da captura
        return exploreQueenPositions(rowOffset + rowDir, colOffset + colDir, nextAcc)
    }
    return if (captor.isQueen) {
        // Para uma rainha, procura múltiplas posições após a captura
        exploreQueenPositions(rowDir, colDir)
    } else {
        // Para peças não-rainhas, calcula apenas uma posição após a captura
        val newRowIdx = captured.pos.row.index + rowDir
        val newColIdx = captured.pos.column.index + colDir

        // Verifica se a peça é uma peça normal e se o movimento está na direção correta
        val correctDirection = when (captor.player) {
            Player.WHITE -> newRowIdx < captor.pos.row.index  // Peça branca vai para cima
            Player.BLACK -> newRowIdx > captor.pos.row.index  // Peça preta vai para baixo
        }

        // Verifica se a nova posição está dentro dos limites do tabuleiro e se está na direção correta
        if (newRowIdx in 0 ..< BOARD_DIM && newColIdx in 0 ..< BOARD_DIM && correctDirection) {
            val singleSquare = Square(Row(newRowIdx), Column(newColIdx))

            // Se a posição está vazia, coloca a peça captora nela
            if (!pieces.containsKey(singleSquare)) {
                mapOf(singleSquare to captor)
            } else {
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }
}