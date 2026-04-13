package pt.isel.daw

import org.junit.jupiter.api.Test
import pt.isel.daw.repo.jdbc.DatabaseConnection
import pt.isel.daw.repo.jdbc.TransactionManagerJdbc
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepositoryJdbcRoundsTest {
    companion object {
        val trxManager = TransactionManagerJdbc()
        val con = DatabaseConnection.getConnection()
    }

    @BeforeTest
    fun setup() {
        val sql =
            """TRUNCATE 
            |dbo.round_turns, dbo.turns, dbo.match_rounds, dbo.matches, 
            |dbo.match_players, dbo.lobbies, dbo.lobby_players, dbo.users 
            |RESTART IDENTITY CASCADE;"""
                .trimMargin()
        con.prepareStatement(sql).use { stmt ->
            stmt.executeUpdate()
        }
        con.commit()
    }

    @Test
    fun `createRound and getById`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 2, host, 2, ante = 5)
            val updateLobby = repoLobbies.addPlayer(lobby.id, player2) ?: lobby
            val match = repoMatches.createMatch(updateLobby) // match starts with one round and one round
            val found = repoMatches.getById(match.id)?.rounds?.get(0)
            val roundGot = repoRounds.getById(found?.id ?: 0)
            val turnsGot = repoTurns.getAll().toMutableList().subList(0, 2)
            val roundCreated = Round(1, 1, 1, turnsGot, host)
            assertEquals(roundCreated, roundGot)
        }
    }

    @Test
    fun `getAll returns all rounds`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 2, host, 2, ante = 5)
            val updateLobby = repoLobbies.addPlayer(lobby.id, player2) ?: lobby
            repoMatches.createMatch(updateLobby)
            val turnsGot1 = repoTurns.getAll().toMutableList().subList(0, 2)
            val turnsGot2 = repoTurns.getAll().toMutableList().subList(2, 4)
            val roundCreated = Round(1, 1, 1, turnsGot1, host)
            val roundCreated2 = Round(2, 1, 2, turnsGot2, host)
            val allRounds = repoRounds.getAll()
            println(allRounds)
            println(listOf(roundCreated, roundCreated2))
            assertEquals(2, allRounds.size)
            assertEquals(listOf(roundCreated, roundCreated2), allRounds)
        }
    }

    @Test
    fun `deleteById removes a round`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 2, host, 2, ante = 5)
            val updateLobby = repoLobbies.addPlayer(lobby.id, player2) ?: lobby
            repoMatches.createMatch(updateLobby)
            val turnsGot1 = repoTurns.getAll().toMutableList().subList(0, 2)
            val turnsGot2 = repoTurns.getAll().toMutableList().subList(2, 4)
            Round(1, 1, 1, turnsGot1, host)
            val roundCreated = Round(2, 1, 2, turnsGot2, host)
            val deleted = repoRounds.deleteById(roundCreated.id)
            val shouldBeNull = repoRounds.getById(roundCreated.id)
            assertEquals(true, deleted)
            assertNull(shouldBeNull)
        }
    }
}
