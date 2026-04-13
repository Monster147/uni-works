package pt.isel.daw

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.isel.daw.repo.RepositoryLobby
import pt.isel.daw.repo.RepositoryUser
import pt.isel.daw.repo.mem.RepositoryLobbiesInMem
import pt.isel.daw.repo.mem.RepositoryUserInMem
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class RepositoryMemLobbiesTest {
    private lateinit var repoUsers: RepositoryUser
    private lateinit var repoLobbies: RepositoryLobby

    @BeforeEach
    fun setup() {
        repoUsers = RepositoryUserInMem()
        repoLobbies = RepositoryLobbiesInMem()
    }

    @Test
    fun `createLobby and getById`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
        val lobbyGot = repoLobbies.getById(lobby.id)
        assertEquals(lobby, lobbyGot)
    }

    @Test
    fun `addPlayer to an Lobby and findByName`() {
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

    @Test
    fun `removePlayer removes a player from a Lobby`() {
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

    @Test
    fun `getAll returns all lobbies`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
        val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
        val lobby2 = repoLobbies.createLobby("Lobby2", "Second Lobby", 5, player2, 8, ante = 5)
        val allLobbies = repoLobbies.getAll()
        assertEquals(2, allLobbies.size)
        assertEquals(listOf(lobby, lobby2), allLobbies)
    }

    @Test
    fun `createLobby and update its name and description and check changes`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
        val found = repoLobbies.getById(lobby.id)
        assertEquals(lobby, found)
        val updatedLobby = lobby.copy(name = "Lobby1Updated", description = "First Lobby Updated")
        repoLobbies.save(updatedLobby)
        val foundUpdated = repoLobbies.getById(lobby.id)
        assertEquals(updatedLobby, foundUpdated)
    }

    @Test
    fun `deleteById removes the lobby`() {
        val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
        val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
        val deleted = repoLobbies.deleteById(lobby.id)
        val shouldBeNull = repoLobbies.getById(lobby.id)
        assertEquals(true, deleted)
        assertNull(shouldBeNull)
    }
}
