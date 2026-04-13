package pt.isel.daw

import org.junit.jupiter.api.Test
import pt.isel.daw.repo.jdbc.DatabaseConnection
import pt.isel.daw.repo.jdbc.TransactionManagerJdbc
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepositoryJdbTurnsTest {
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
    fun `createTurn and getById`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 2, host, 2, ante = 5)
            val updateLobby = repoLobbies.addPlayer(lobby.id, player2) ?: lobby
            val match = repoMatches.createMatch(updateLobby) // match starts with one round and one turn
            val found = repoMatches.getById(match.id)?.rounds?.get(0)?.turns?.get(0)
            val turnGot = repoTurns.getById(found?.id ?: 0)
            val turnCreated = Turn(1, 1, host, Hand(), 0)
            assertEquals(turnCreated, turnGot)
        }
    }

    @Test
    fun `getAll returns all turns`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 2, host, 2, ante = 5)
            val updateLobby = repoLobbies.addPlayer(lobby.id, player2) ?: lobby
            repoMatches.createMatch(updateLobby)
            val turnCreated = Turn(1, 1, host, Hand(), 0)
            val turnCreated2 = Turn(2, 1, player2, Hand(), 0)
            val turnCreated3 = Turn(3, 2, host, Hand(), 0)
            val turnCreated4 = Turn(4, 2, player2, Hand(), 0)
            val allTurns = repoTurns.getAll()
            println(allTurns)
            assertEquals(4, allTurns.size)
            assertEquals(listOf(turnCreated, turnCreated2, turnCreated3, turnCreated4), allTurns)
        }
    }

    @Test
    fun `deleteById removes a turn`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 2, host, 2, ante = 5)
            val updateLobby = repoLobbies.addPlayer(lobby.id, player2) ?: lobby
            repoMatches.createMatch(updateLobby)
            Turn(1, 1, host, Hand(), 1)
            val turnCreated = Turn(2, 1, player2, Hand(), 0)
            val deleted = repoTurns.deleteById(turnCreated.id)
            val shouldBeNull = repoTurns.getById(turnCreated.id)
            assertEquals(true, deleted)
            assertNull(shouldBeNull)
        }
    }
}
