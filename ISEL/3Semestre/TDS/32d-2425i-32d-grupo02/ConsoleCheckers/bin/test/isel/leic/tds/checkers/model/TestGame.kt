package isel.leic.tds.checkers.model

import kotlin.test.*

// All tests assume BOARD_DIM = 8
class TestGame {
    @Test fun `Game initializes with default values`() {
        val game = Game()
        assertNull(game.board, "Board should be null")
        assertEquals(Player.WHITE, game.firstPlayer, "First player should be WHITE by default")
        assertEquals(0, game.score[Player.WHITE], "Score for WHITE should start at 0")
        assertEquals(0, game.score[Player.BLACK], "Score for BLACK should start at 0")
    }

    @Test fun `New Board and switches first player`() {
        val initialGame = Game()
        val newGame = initialGame.newBoard()

        assertNotNull(newGame.board, "newBoard should create a new board")
        assertEquals(initialGame.firstPlayer.other, newGame.firstPlayer, "newBoard should switch the first player")
    }

    @Test fun `Play results in WinnerBoard and updates score`() {
        val initialBoard = mapOf(Square(Row(0), Column(1)) to Piece(Player.BLACK, Square(Row(0), Column(1))))
        val game = Game(board = BoardPlaying(initialBoard, Player.BLACK))
        val newGame = game.play(Square(Row(0), Column(1)), Square(Row(1), Column(2)))
        assertTrue(newGame.board is WinnerBoard, "Game should end in a WinnerBoard when player wins")
        assertEquals(1, newGame.score[Player.BLACK], "Score for WHITE should increment after winning")
    }

    @Test fun `Play results in DrawBoard and updates score`() {
        val movesWithoutCapture = DRAW_TURNS - 1
        val game = Game(
            board = BoardPlaying(Board.initialBoard, Player.WHITE, movesWithoutCapture),
            score = mapOf(Player.WHITE to 0, Player.BLACK to 0, null to 0)
        )
        val newGame = game.play(Square(Row(5), Column(0)), Square(Row(4), Column(1)))
        assertTrue(newGame.board is DrawBoard, "Game should end in DrawBoard after sufficient moves without capture")
        assertEquals(1, newGame.score[null], "Score for draws should increment after draw")
    }
}