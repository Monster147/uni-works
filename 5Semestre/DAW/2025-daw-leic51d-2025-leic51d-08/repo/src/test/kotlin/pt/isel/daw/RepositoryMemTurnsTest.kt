package pt.isel.daw

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.isel.daw.repo.RepositoryLobby
import pt.isel.daw.repo.RepositoryMatches
import pt.isel.daw.repo.RepositoryRounds
import pt.isel.daw.repo.RepositoryTurns
import pt.isel.daw.repo.RepositoryUser
import pt.isel.daw.repo.mem.RepositoryLobbiesInMem
import pt.isel.daw.repo.mem.RepositoryMatchesInMem
import pt.isel.daw.repo.mem.RepositoryRoundsInMem
import pt.isel.daw.repo.mem.RepositoryTurnsInMem
import pt.isel.daw.repo.mem.RepositoryUserInMem
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepositoryMemTurnsTest {
    private lateinit var repoUsers: RepositoryUser
    private lateinit var repoLobbies: RepositoryLobby
    private lateinit var repoMatches: RepositoryMatches
    private lateinit var repoRounds: RepositoryRounds
    private lateinit var repoTurns: RepositoryTurns

    @BeforeEach
    fun setup() {
        repoUsers = RepositoryUserInMem()
        repoLobbies = RepositoryLobbiesInMem()
        repoMatches = RepositoryMatchesInMem()
        repoRounds = RepositoryRoundsInMem()
        repoTurns = RepositoryTurnsInMem()
    }

    @Test
    fun `createTurn and getById`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val turnCreated = repoTurns.createTurn(Turn(1, 1, host, Hand(), 1))
        val turnGot = repoTurns.getById(turnCreated.id)
        assertEquals(turnCreated, turnGot)
    }

    @Test
    fun `getAll returns all turns`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
        val turnCreated = repoTurns.createTurn(Turn(1, 1, host, Hand(), 1))
        val turnCreated2 = repoTurns.createTurn(Turn(2, 1, player2, Hand(), 1))
        val turnCreated3 = repoTurns.createTurn(Turn(3, 2, host, Hand(), 1))
        val turnCreated4 = repoTurns.createTurn(Turn(4, 2, player2, Hand(), 1))
        val allTurns = repoTurns.getAll()
        assertEquals(4, allTurns.size)
        assertEquals(listOf(turnCreated, turnCreated2, turnCreated3, turnCreated4), allTurns)
    }

    @Test
    fun `createTurn and update its hand and state`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val turnCreated = repoTurns.createTurn(Turn(1, 1, host, Hand(), 1))
        val found = repoTurns.getById(turnCreated.id)
        assertEquals(turnCreated, found)
        val updatedTurn = turnCreated.copy(hand = Hand(listOf(Dice(DiceFace.ACE), Dice(DiceFace.KING))))
        repoTurns.save(updatedTurn)
        val foundUpdated = repoTurns.getById(turnCreated.id)
        assertEquals(updatedTurn, foundUpdated)
    }

    @Test
    fun `deleteById removes a turn`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val turn = repoTurns.createTurn(Turn(1, 1, host, Hand(), 1))
        val turnGot = repoTurns.getById(turn.id)
        assertEquals(turn, turnGot)
        val deleted = repoTurns.deleteById(turn.id)
        val shouldBeNull = repoTurns.getById(turn.id)
        assertEquals(true, deleted)
        assertNull(shouldBeNull)
    }
}
