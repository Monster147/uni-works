import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import pt.isel.daw.Dice
import pt.isel.daw.DiceFace
import pt.isel.daw.Hand
import pt.isel.daw.Lobby
import pt.isel.daw.LobbyState
import pt.isel.daw.Match
import pt.isel.daw.MatchState
import pt.isel.daw.PasswordValidationInfo
import pt.isel.daw.User
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals

private fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

class DomainTest {
    private val host =
        User(
            1,
            "Alice",
            "alice@example.com",
            PasswordValidationInfo(newTokenValidationData()),
        )
    private val player =
        User(
            2,
            "Bob",
            "bob@example.com",
            PasswordValidationInfo(newTokenValidationData()),
        )
    private val player2 =
        User(
            3,
            "Cartman",
            "cartman@example.com",
            PasswordValidationInfo(newTokenValidationData()),
        )
    private val lobbyStart =
        Lobby(
            1,
            "Test Lobby",
            "A lobby for testing",
            5,
            host,
            10,
            listOf(host, player, player2),
        )
    private val match = Match(1, lobbyStart.id, lobbyStart.players, listOf(), 0)

    @Test
    fun `test dice face`() {
        val face = Dice(DiceFace.ACE)
        assertEquals(DiceFace.ACE, face.face)
    }

    @Test
    fun `test hand creation`() {
        val dice1 = Dice(DiceFace.ACE)
        val dice2 = Dice(DiceFace.KING)
        val dice3 = Dice(DiceFace.QUEEN)
        val hand = Hand(listOf(dice1, dice2, dice3))
        assertEquals(3, hand.dice.size)
    }

    @Test
    fun `test lobby initial state`() {
        assertEquals(lobbyStart.state, LobbyState.OPEN)
    }

    @Test
    fun `test lobby creation`() {
        assertDoesNotThrow {
            Lobby(1, "Valid Name", "Valid Description", 5, host, 10, listOf(host))
        }
    }

    @Test
    fun `test lobby with invalid name`() {
        try {
            Lobby(1, "", "A lobby with invalid name", 5, host, 10, listOf(host))
        } catch (e: IllegalArgumentException) {
            assertEquals("Name must not be blank", e.message)
        }
    }

    @Test
    fun `test lobby with invalid description`() {
        try {
            Lobby(1, "Valid Name", "", 5, host, 10, listOf(host))
        } catch (e: IllegalArgumentException) {
            assertEquals("Description must not be blank", e.message)
        }
    }

    @Test
    fun `test lobby with invalid max players (bellow)`() {
        try {
            Lobby(1, "Valid Name", "Valid Description", 1, host, 10, listOf(host))
        } catch (e: IllegalArgumentException) {
            assertEquals("Max players must be between 2 and 10", e.message)
        }
    }

    @Test
    fun `test lobby with invalid max players (above)`() {
        try {
            Lobby(1, "Valid Name", "Valid Description", 11, host, 10, listOf(host))
        } catch (e: IllegalArgumentException) {
            assertEquals("Max players must be between 2 and 10", e.message)
        }
    }

    @Test
    fun `test match initial state`() {
        assertEquals(MatchState.IN_PROGRESS, match.state)
    }

    @Test
    fun `test initial player balance`() {
        assertEquals(500, host.balance)
    }

    @Test
    fun `test create a user`() {
        assertDoesNotThrow {
            User(
                5,
                "Eve",
                "eve@example.com",
                PasswordValidationInfo(
                    newTokenValidationData(),
                ),
            )
        }
    }

    @Test
    fun `test create a user with invalid name`() {
        try {
            User(
                6,
                "E",
                "e@example.com",
                PasswordValidationInfo(
                    newTokenValidationData(),
                ),
            )
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid name format", e.message)
        }
    }

    @Test
    fun `test create a user with invalid email`() {
        try {
            User(
                7,
                "Eve",
                "invalid-email",
                PasswordValidationInfo(
                    newTokenValidationData(),
                ),
            )
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid email format", e.message)
        }
    }
}
