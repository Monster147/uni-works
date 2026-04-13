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

class RepositoryMemRoundsTest {
    private lateinit var repoRounds: RepositoryRounds
    private lateinit var repoUsers: RepositoryUser
    private lateinit var repoLobbies: RepositoryLobby
    private lateinit var repoMatches: RepositoryMatches
    private lateinit var repoTurns: RepositoryTurns

    @BeforeEach
    fun setup() {
        repoRounds = RepositoryRoundsInMem()
        repoUsers = RepositoryUserInMem()
        repoLobbies = RepositoryLobbiesInMem()
        repoMatches = RepositoryMatchesInMem()
        repoTurns = RepositoryTurnsInMem()
    }

    @Test
    fun `createRound and getById`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
        val turns =
            mutableListOf(
                Turn(1, 1, host, Hand()),
                Turn(2, 1, player2, Hand()),
            )
        val roundCreated = repoRounds.createRound(Round(1, 1, 1, turns, host))
        val roundGot = repoRounds.getById(roundCreated.id)
        assertEquals(roundCreated, roundGot)
    }

    @Test
    fun `getAll returns all rounds`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
        val turns1 =
            mutableListOf(
                Turn(1, 1, host, Hand()),
                Turn(2, 1, player2, Hand()),
            )
        val turns2 =
            mutableListOf(
                Turn(3, 2, host, Hand()),
                Turn(4, 2, player2, Hand()),
            )
        val round = repoRounds.createRound(Round(1, 1, 1, turns1, host))
        val round2 = repoRounds.createRound(Round(2, 1, 2, turns2, host))
        val allRounds = repoRounds.getAll()
        assertEquals(2, allRounds.size)
        assertEquals(listOf(round, round2), allRounds)
    }

    @Test
    fun `createRound and update its turns`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
        val turns =
            mutableListOf(
                Turn(1, 1, host, Hand()),
                Turn(2, 1, player2, Hand()),
            )
        val roundCreated = repoRounds.createRound(Round(1, 1, 1, turns, host))
        val found = repoRounds.getById(roundCreated.id)
        assertEquals(roundCreated, found)
        val uptdTurns =
            mutableListOf(
                Turn(1, 1, host, Hand(), 1, TurnState.COMPLETED, HandCategory.ONE_PAIR),
                Turn(2, 1, player2, Hand(), 1, TurnState.IN_PROGRESS, HandCategory.FULL_HOUSE),
            )
        val updatedRound = roundCreated.copy(turns = uptdTurns)
        repoRounds.save(updatedRound)
        val foundUpdated = repoRounds.getById(roundCreated.id)
        assertEquals(updatedRound, foundUpdated)
    }

    @Test
    fun `deleteById removes a round`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
        val turns =
            mutableListOf(
                Turn(1, 1, host, Hand()),
                Turn(2, 1, player2, Hand()),
            )
        val roundCreated = repoRounds.createRound(Round(1, 1, 1, turns, host))
        val deleted = repoRounds.deleteById(roundCreated.id)
        val shouldBeNull = repoRounds.getById(roundCreated.id)
        assertEquals(true, deleted)
        assertNull(shouldBeNull)
    }
}
