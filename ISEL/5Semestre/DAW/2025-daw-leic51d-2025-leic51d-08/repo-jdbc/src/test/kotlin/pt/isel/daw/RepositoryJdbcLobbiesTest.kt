package pt.isel.daw

import org.junit.jupiter.api.Test
import pt.isel.daw.repo.jdbc.DatabaseConnection
import pt.isel.daw.repo.jdbc.TransactionManagerJdbc
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class RepositoryJdbcLobbiesTest {
    companion object {
        val trxManager = TransactionManagerJdbc()
        val con = DatabaseConnection.getConnection()
    }

    @BeforeTest
    fun setup() {
        val sql = "TRUNCATE dbo.lobbies, dbo.lobby_players, dbo.users RESTART IDENTITY CASCADE;"
        con.prepareStatement(sql).use { stmt ->
            stmt.executeUpdate()
        }
        con.commit()
    }

    @Test
    fun `createLobby and getById`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
            val lobbyGot = repoLobbies.getById(lobby.id)
            assertEquals(lobby, lobbyGot)
        }
    }

    @Test
    fun `addPlayer to an Lobby and findByName`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
            val found = repoLobbies.findByName("Lobby1")
            assertEquals(lobby, found)
            repoLobbies.addPlayer(lobby.id, player2)
            val updatedLobby = repoLobbies.getById(lobby.id)
            assertEquals(2, updatedLobby?.players?.size)
            assertEquals(listOf(host, player2), updatedLobby?.players)
            assertNotSame(found, updatedLobby)
        }
    }

    @Test
    fun `removePlayer removes a player from a Lobby`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
            repoLobbies.addPlayer(lobby.id, player2)
            val lobbyGot = repoLobbies.getById(lobby.id)
            assertNotSame(lobby, lobbyGot)
            val removed = repoLobbies.removePlayer(lobby.id, player2)
            assertEquals(lobby, removed)
            assertNotSame(listOf(host, player2), removed?.players)
        }
    }

    @Test
    fun `getAll returns all lobbies`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
            val lobby2 = repoLobbies.createLobby("Lobby2", "Second Lobby", 5, player2, 8, ante = 5)
            val allLobbies = repoLobbies.getAll()
            assertEquals(2, allLobbies.size)
            assertEquals(listOf(lobby, lobby2), allLobbies)
        }
    }

    @Test
    fun `getAllAvailable does not include full lobbies`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val p2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val p3 = repoUsers.createUser("Carl", "carl@isel.com", PasswordValidationInfo("hash3"))
            val p4 = repoUsers.createUser("Dana", "dana@isel.com", PasswordValidationInfo("hash4"))
            val fullLobby = repoLobbies.createLobby("FullLobby", "A full lobby", 4, host, 10, ante = 5)
            repoLobbies.addPlayer(fullLobby.id, p2)
            repoLobbies.addPlayer(fullLobby.id, p3)
            repoLobbies.addPlayer(fullLobby.id, p4)
            val host2 = repoUsers.createUser("Eve", "eve@example.com", PasswordValidationInfo("hash5"))
            val openLobby = repoLobbies.createLobby("OpenLobby", "Not full", 5, host2, 8, ante = 5)
            val allLobbies = repoLobbies.getAllAvailable()
            assertEquals(1, allLobbies.size)
            assertEquals(listOf(openLobby), allLobbies)
        }
    }

    @Test
    fun `createLobby and update its name and description and check changes`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
            val found = repoLobbies.getById(lobby.id)
            assertEquals(lobby, found)
            val updatedLobby = lobby.copy(name = "Lobby1Updated", description = "First Lobby Updated")
            repoLobbies.save(updatedLobby)
            val foundUpdated = repoLobbies.getById(lobby.id)
            assertEquals(updatedLobby, foundUpdated)
        }
    }

    @Test
    fun `deleteById removes the lobby`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
            val deleted = repoLobbies.deleteById(lobby.id)
            val shouldBeNull = repoLobbies.getById(lobby.id)
            assertEquals(true, deleted)
            assertNull(shouldBeNull)
        }
    }
}
