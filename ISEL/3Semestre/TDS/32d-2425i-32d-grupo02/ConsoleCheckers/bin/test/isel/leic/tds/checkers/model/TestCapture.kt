package isel.leic.tds.checkers.model

import kotlin.test.*
import isel.leic.tds.checkers.model.*

// All tests assume BOARD_DIM = 8
class TestCapture {
    @Test fun `Existing valid captures`() {
        val board = mapOf(
            Square(Row(3), Column(2)) to Piece(Player.WHITE, Square(Row(3), Column(2))),
            Square(Row(2), Column(3)) to Piece(Player.BLACK, Square(Row(2), Column(3)))
        )
        val player = Player.WHITE
        val possibleCaptures = getPossibleCaptures(player, board)
        assertEquals(1, possibleCaptures.size, "Should be 1 valid capture")
        assertEquals(Square(Row(1), Column(4)), possibleCaptures[0].pos, "Capture should land on the correct position")
        assertEquals(player, possibleCaptures[0].captor?.player, "Captor should be the correct player")
        assertEquals(Player.BLACK, possibleCaptures[0].captured?.player, "Captured should be the correct enemy piece")
    }

    @Test fun `Existing no valid captures`() {
        val board = mapOf(
            Square(Row(2), Column(1)) to Piece(Player.WHITE, Square(Row(2), Column(1))),
            Square(Row(4), Column(5)) to Piece(Player.BLACK, Square(Row(4), Column(5)))
        )
        val player = Player.WHITE
        val possibleCaptures = getPossibleCaptures(player, board)
        assertEquals(0, possibleCaptures.size, "There should be no valid captures")
    }

    @Test fun `Get position after capture for normal piece`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(3), Column(2)))
        val blackPiece = Piece(Player.BLACK, Square(Row(2), Column(3)))
        val board = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece
        )
        val possiblePositions = getPossiblePosAfterCapture(whitePiece, blackPiece, board)
        assertTrue(possiblePositions.isNotEmpty(), "There should be a valid position for capture")
        assertEquals(Square(Row(1), Column(4)), possiblePositions.keys.first(), "Capture should land on row 4, column 4")
    }

    @Test fun `Get position after capture for queen piece`() {
        val whiteQueen = Piece(Player.WHITE, Square(Row(2), Column(1)), isQueen = true)
        val blackPiece = Piece(Player.BLACK, Square(Row(3), Column(2)))
        val board = mapOf(
            whiteQueen.pos to whiteQueen,
            blackPiece.pos to blackPiece
        )
        val possiblePositions = getPossiblePosAfterCapture(whiteQueen, blackPiece, board)
        assertTrue(possiblePositions.isNotEmpty(), "Queen should have multiple capture options")
    }

    @Test fun `Get position after capture for invalid capture move`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(2), Column(3)))
        val blackPiece = Piece(Player.BLACK, Square(Row(1), Column(0)))
        val board = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece
        )
        val possiblePositions = getPossiblePosAfterCapture(whitePiece, blackPiece, board)
        assertTrue(possiblePositions.isEmpty(), "There should be no valid capture for this direction")
    }

    @Test fun `Get position after capture for piece out of bounds`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(0), Column(7)))
        val blackPiece = Piece(Player.BLACK, Square(Row(1), Column(6)))
        val board = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece
        )
        val possiblePositions = getPossiblePosAfterCapture(whitePiece, blackPiece, board)
        assertTrue(possiblePositions.isEmpty(), "Capture move should not go out of bounds")
    }

    @Test fun `Existing captures for queen - capturing multiple pieces`() {
        val whiteQueen = Piece(Player.WHITE, Square(Row(4), Column(1)), isQueen = true)
        val blackPiece1 = Piece(Player.BLACK, Square(Row(3), Column(2)))
        val blackPiece2 = Piece(Player.BLACK, Square(Row(1), Column(2)))
        val board = mapOf(
            whiteQueen.pos to whiteQueen,
            blackPiece1.pos to blackPiece1,
            blackPiece2.pos to blackPiece2
        )
        val possibleCaptures = getPossibleCaptures(Player.WHITE, board)
        assertTrue(possibleCaptures.size >= 2, "Queen should be able to capture multiple pieces")
    }

    @Test fun `Existing captures with empty board`() {
        val board = emptyMap<Square, Piece>()
        val player = Player.WHITE
        val possibleCaptures = getPossibleCaptures(player, board)
        assertEquals(0, possibleCaptures.size, "There should be no captures on an empty board")
    }

    @Test fun `Get position after capture for piece attempting illegal move`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(2), Column(3)))
        val blackPiece = Piece(Player.BLACK, Square(Row(3), Column(2)))
        val board = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece
        )
        val possiblePositions = getPossiblePosAfterCapture(whitePiece, blackPiece, board)
        assertTrue(possiblePositions.isEmpty(), "The move should not be legal")
    }
}