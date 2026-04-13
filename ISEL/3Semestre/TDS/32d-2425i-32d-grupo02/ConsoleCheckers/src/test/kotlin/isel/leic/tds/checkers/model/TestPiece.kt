package isel.leic.tds.checkers.model

import kotlin.test.*

// All tests assume BOARD_DIM = 8
class TestPiece {
    @Test fun `Symbol for normal Piece`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(2), Column(1)))
        val blackPiece = Piece(Player.BLACK, Square(Row(2), Column(3)))
        assertEquals('w', whitePiece.symbol, "White piece symbol should be 'W'")
        assertEquals('b', blackPiece.symbol, "Black piece symbol should be 'B'")
    }

    @Test fun `Symbol for queen Piece`() {
        val whiteQueen = Piece(Player.WHITE, Square(Row(2), Column(1)), isQueen = true)
        val blackQueen = Piece(Player.BLACK, Square(Row(2), Column(3)), isQueen = true)
        assertEquals('W', whiteQueen.symbol, "White queen symbol should be 'w'")
        assertEquals('B', blackQueen.symbol, "Black queen symbol should be 'b'")
    }

    @Test fun `White pawn piece becomes White queen piece`() {
        val rowAtTop = Row(0)
        val rowNotAtTop = Row(1)
        val whitePiece = Piece(Player.WHITE, Square(rowAtTop, Column(1)))
        assertTrue(whitePiece.canBeQueen(rowAtTop), "White piece should be able to become a queen at the top row")
        assertFalse(whitePiece.canBeQueen(rowNotAtTop), "White piece should not be able to become a queen at a non-top row")
    }

    @Test fun `Black pawn piece becomes Black queen piece`() {
        val rowAtBottom = Row(BOARD_DIM - 1)
        val rowNotAtBottom = Row(BOARD_DIM - 2)
        val blackPiece = Piece(Player.BLACK, Square(rowAtBottom, Column(0)))
        assertTrue(blackPiece.canBeQueen(rowAtBottom), "Black piece should be able to become a queen at the bottom row")
        assertFalse(blackPiece.canBeQueen(rowNotAtBottom), "Black piece should not be able to become a queen at a non-bottom row")
    }

    @Test fun `Valid capture move by non-queen piece`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(2), Column(1)))
        val blackPiece = Piece(Player.BLACK, Square(Row(1), Column(2)))
        val places = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece
        )
        assertTrue(whitePiece.canCapture(blackPiece, places), "White piece should be able to capture the black piece")
    }

    @Test fun `Invalid capture move by non-queen piece`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(2), Column(1)))
        val blackPiece = Piece(Player.BLACK, Square(Row(2), Column(3)))
        val places = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece
        )
        assertFalse(whitePiece.canCapture(blackPiece, places), "White piece should not be able to capture a piece that is not diagonally adjacent")
    }

    @Test fun `Valid capture move by queen piece`() {
        val whiteQueen = Piece(Player.WHITE, Square(Row(2), Column(1)), isQueen = true)
        val blackPiece = Piece(Player.BLACK, Square(Row(3), Column(2)))
        val places = mapOf(
            whiteQueen.pos to whiteQueen,
            blackPiece.pos to blackPiece
        )
        assertTrue(whiteQueen.canCapture(blackPiece, places), "Queen should be able to capture diagonally")
    }

    @Test fun `A piece is guarding another`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(4), Column(1)))
        val blackPiece = Piece(Player.BLACK, Square(Row(3), Column(2)))
        val blackGuard = Piece(Player.BLACK, Square(Row(2), Column(3)))
        val places = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece,
            blackGuard.pos to blackGuard
        )
        assertFalse(whitePiece.canCapture(blackPiece, places), "White piece should not be able to capture the black piece because it is guarded")
    }

    @Test fun `Capture move for non-capturable piece (same player)`() {
        val whitePiece1 = Piece(Player.WHITE, Square(Row(2), Column(1)))
        val whitePiece2 = Piece(Player.WHITE, Square(Row(3), Column(4))) // Same player
        val places = mapOf(
            whitePiece1.pos to whitePiece1,
            whitePiece2.pos to whitePiece2
        )
        assertFalse(whitePiece1.canCapture(whitePiece2, places), "White piece should not be able to capture another white piece")
    }

    @Test fun `Capture move for non-capturable piece (same position)`(){
        val whitePiece = Piece(Player.WHITE, Square(Row(2), Column(1)))
        val blackPiece = Piece(Player.BLACK, Square(Row(2), Column(1)))
        val places = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece
        )
        assertFalse(whitePiece.canCapture(blackPiece, places), "White piece should not be able to capture a piece at the same position")
    }

    @Test fun `Capture move for piece at board edge (Black piece captures white piece)`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(1), Column(6)))
        val blackPiece = Piece(Player.BLACK, Square(Row(0), Column(7)))
        val places = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece
        )
        assertTrue(blackPiece.canCapture(whitePiece, places), "Black piece should be able to capture white piece diagonally")
    }

    @Test fun `Capture move for piece at board corner (White piece captures black piece)`() {
        val whitePiece = Piece(Player.WHITE, Square(Row(1), Column(7)))
        val blackPiece = Piece(Player.BLACK, Square(Row(0), Column(7)))
        val places = mapOf(
            whitePiece.pos to whitePiece,
            blackPiece.pos to blackPiece
        )
        assertFalse(whitePiece.canCapture(blackPiece, places), "White piece should not be able to capture black piece diagonally")
    }
}