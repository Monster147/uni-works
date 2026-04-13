package isel.leic.tds.storage

import kotlin.test.*


import isel.leic.tds.checkers.model.*
import isel.leic.tds.checkers.model.Player.BLACK
import isel.leic.tds.checkers.model.Player.WHITE

class SerializerTests {
    @Test fun `serialize and deserialize BoardRun`() {
        val pieces = mapOf(
            Square(Row(6), Column(1)) to Piece(WHITE, Square(Row(6), Column(1))),
            Square(Row(5), Column(2)) to Piece(BLACK, Square(Row(5), Column(2)))
        )
        val board = BoardPlaying(pieces, BLACK)
        val text = BoardSerializer.serialize(board)
        assertEquals("RUN BLACK 0 | WHITE/2b/false BLACK/3c/false", text)
        val otherBoard = BoardSerializer.deserialize(text)
        assertEquals(board, otherBoard)
    }
    @Test fun `serialize and deserialize BoardDraw`() {
        val pieces = mapOf(
            Square(Row(5), Column(0)) to Piece(WHITE, Square(Row(5), Column(0))),
            Square(Row(5), Column(2)) to Piece(BLACK, Square(Row(5), Column(2)))
        )
        val from = Square(Row(5), Column(0))
        val to = Square(Row(4), Column(1))
        val board = BoardPlaying(pieces, WHITE, 19).play(from, to)
        val text = BoardSerializer.serialize(board)
        assertEquals("DRAW null null | BLACK/3c/false WHITE/4b/false", text)
        val otherBoard = BoardSerializer.deserialize(text)
        assertEquals(board, otherBoard)
    }
    @Test fun `serialize and deserialize BoardWin`() {
        val pieces = mapOf(
            Square(Row(6), Column(1)) to Piece(WHITE, Square(Row(6), Column(1))),
            Square(Row(5), Column(2)) to Piece(BLACK, Square(Row(5), Column(2)))
        )
        val from = Square(Row(6), Column(1))
        val to = Square(Row(4), Column(3))
        val board = BoardPlaying(pieces, WHITE, 1).play(from, to)
        val text = BoardSerializer.serialize(board)
        assertEquals("WIN WHITE 1 | WHITE/4d/false", text)
        val otherBoard = BoardSerializer.deserialize(text)
        assertEquals(board, otherBoard)
    }
    @Test fun `serialize and deserialize Game`() {
        val pieces = mapOf(
            Square(Row(6), Column(1)) to Piece(WHITE, Square(Row(6), Column(1))),
            Square(Row(5), Column(2)) to Piece(BLACK, Square(Row(5), Column(2)))
        )
        val game = Game(
            board = BoardPlaying(pieces, BLACK),
            firstPlayer = BLACK,
            score = mapOf(BLACK to 2, WHITE to 1, null to 4)
        )
        val text = GameSerializer.serialize(game)
        assertEquals("BLACK # BLACK:2 WHITE:1 null:4 # RUN BLACK 0 | WHITE/2b/false BLACK/3c/false", text)
        val otherGame = GameSerializer.deserialize(text)
        assertEquals(game, otherGame)
    }
}