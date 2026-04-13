package isel.leic.tds.checkers.model

import isel.leic.tds.checkers.model.Player.BLACK
import isel.leic.tds.checkers.model.Player.WHITE
import java.lang.*
import kotlin.test.*

// All tests assume BOARD_DIM = 8
class TestBoard {
    @Test fun `Game ended with a Win`(){
        val board = WinnerBoard(emptyMap(), BLACK, 0)
        val square = "5b".toSquareOrNull()
        assertNotNull(square)
        val ex = assertFailsWith<IllegalStateException> { board.play(square, square) }.message
        assertEquals("Game Over: The player BLACK already won this game", ex)
    }

    @Test fun `Game ended with a Draw`(){
        val board = DrawBoard(emptyMap())
        val square = "5b".toSquareOrNull()
        assertNotNull(square)
        val ex = assertFailsWith<IllegalStateException> { board.play(square, square) }.message
        assertEquals("Game Over: The game already ended in a draw", ex)
    }

    @Test fun `Initial board setup`() {
        val board = BoardPlaying(currPlayer = WHITE)
        val whitePieces = board.places.values.filter { it.player == WHITE }
        val blackPieces = board.places.values.filter { it.player == BLACK }
        assertEquals(12, whitePieces.size, "White should start with 12 pieces")
        assertEquals(12, blackPieces.size, "Black should start with 12 pieces")
        whitePieces.forEach {
            assertTrue(it.pos.row.index >= BOARD_DIM / 2 + 1)
        }
        blackPieces.forEach {
            assertTrue(it.pos.row.index < BOARD_DIM / 2 - 1)
        }
    }

    @Test fun `Move with no capture`() {
        val board = BoardPlaying(currPlayer = WHITE)
        val from = Square(Row(5), Column(0))
        val to = Square(Row(4), Column(1))
        val updatedBoard = board.play(from, to) as BoardPlaying
        assertEquals(BLACK, updatedBoard.currPlayer, "Current player should be BLACK after White's turn")
        assertTrue(updatedBoard.places.containsKey(to), "Piece should have moved to the new position")
        assertTrue(!updatedBoard.places.containsKey(from), "Original position should now be empty")
    }

    @Test fun `Invalid move to occupied Square`() {
        val board = BoardPlaying(currPlayer = WHITE)
        val from = Square(Row(5), Column(0))
        val to = Square(Row(6), Column(1))
        assertFailsWith<IllegalArgumentException> {
            board.play(from, to)
        }
    }

    @Test fun `Piece turning into a Queen`() {
        val places = mapOf(
            Square(Row(6), Column(1)) to Piece(BLACK, Square(Row(6), Column(1)), false)
        )
        val from = Square(Row(6), Column(1))
        val to = Square(Row(7), Column(0))
        val board = BoardPlaying(places, currPlayer = BLACK).play(from, to)
        val checkQueen = board.places[to]?.isQueen
        assertNotNull(checkQueen)
        assertTrue(checkQueen)
    }

    @Test fun `Capture`() {
        var board = BoardPlaying(currPlayer = WHITE)
        board = board.play(Square(Row(5), Column(0)), Square(Row(4), Column(1))) as BoardPlaying
        board = board.play(Square(Row(2), Column(3)), Square(Row(3), Column(2))) as BoardPlaying
        val from = Square(Row(4), Column(1))
        val to = Square(Row(2), Column(3))
        val updatedBoard = board.play(from, to) as BoardPlaying
        assertEquals(BLACK, updatedBoard.currPlayer, "Current player should change to Black after capture")
        assertTrue(!updatedBoard.places.containsKey(Square(Row(3), Column(2))), "Captured piece should be removed")
    }

    @Test fun `Mandatory Capture`() {
        var board = BoardPlaying(currPlayer = WHITE)
        board = board.play(Square(Row(5), Column(0)), Square(Row(4), Column(1)))  as BoardPlaying
        board = board.play(Square(Row(2), Column(1)), Square(Row(3), Column(2)))  as BoardPlaying
        val invalidMove = Square(Row(5), Column(2))
        assertFailsWith<IllegalArgumentException> {
            board.play(Square(Row(4), Column(1)), invalidMove)
        }
    }

    @Test fun `Game over by winning`() {
        val pieces = mapOf(
            Square(Row(6), Column(1)) to Piece(WHITE, Square(Row(6), Column(1))),
            Square(Row(5), Column(2)) to Piece(BLACK, Square(Row(5), Column(2)))
        )
        val board = BoardPlaying(pieces, WHITE).play(Square(Row(6), Column(1)), Square(Row(4), Column(3)))
        assertTrue(board is WinnerBoard)
        assertEquals(WHITE, board.winner)
    }

    @Test fun `Draw condition after multiple moves with no capture`() {
        val from = Square(Row(5), Column(0))
        val to = Square(Row(4), Column(1))
        val board = BoardPlaying(currPlayer = WHITE, movesWithoutCapture = DRAW_TURNS - 1).play(from, to)
        assertTrue(board is DrawBoard, "Game should end in a draw after $DRAW_TURNS moves without capture")
    }
}
