package pt.isel.daw.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.isel.daw.PasswordValidationInfo
import pt.isel.daw.User
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringJUnitConfig(TestConfig::class)
class TestLobbyServices {
    @Autowired
    private lateinit var service: LobbyServices

    private fun createUser(
        name: String,
        id: Int = (1..1000).random(),
    ) = User(id = id, name = name, email = "$name@isel.pt", passwordValidation = PasswordValidationInfo("hashedPwd"))

    @Test
    fun `createLobby creates new lobby successfully`() {
        val host = createUser("Alice")
        val lobby =
            service.createLobby(
                name = "CoolLobby",
                description = "Fun game lobby",
                maxPlayers = 4,
                host = host,
                rounds = 3,
                ante = 2,
            ).let {
                check(it is Success)
                it.value
            }
        assertEquals("CoolLobby", lobby.name)
        assertEquals(host.id, lobby.host.id)
        assertEquals(4, lobby.maxPlayers)
    }

    @Test
    fun `createLobby fails if name already exists`() {
        val host = createUser("Bob")
        service.createLobby("DuplicateLobby", "Desc", 4, host, 2, 1)
        val result =
            service.createLobby("DuplicateLobby", "Another", 4, host, 2, 1)
                .let {
                    check(it is Failure)
                    it.value
                }

        assertTrue(result is LobbyError.LobbyNameAlreadyInUse)
    }

    @Test
    fun `joinLobby adds player successfully`() {
        val host = createUser("Carol")
        val lobby =
            service.createLobby("JoinLobby", "Join test", 4, host, 3, 1).let {
                check(it is Success)
                it.value
            }
        val player = createUser("Dave")
        val updatedLobby =
            service.joinLobby(lobby.id, player).let {
                check(it is Success)
                it.value
            }
        assertTrue(updatedLobby.players.any { it.id == player.id })
    }

    @Test
    fun `joinLobby fails for non-existent lobby`() {
        val player = createUser("Eve")
        val result =
            service.joinLobby(999, player).let {
                check(it is Failure)
                it.value
            }
        assertTrue(result is LobbyError.NotFound)
    }

    @Test
    fun `leaveLobby removes player successfully`() {
        val host = createUser("Frank")
        val lobby =
            service.createLobby("LeaveLobby", "Leave test", 4, host, 3, 1).let {
                check(it is Success)
                it.value
            }
        val player = createUser("Grace")
        service.joinLobby(lobby.id, player)
        val updatedLobby =
            service.leaveLobby(lobby.id, player).let {
                check(it is Success)
                it.value
            }
        assertTrue(updatedLobby.players.none { it.id == player.id })
    }

    @Test
    fun `leaveLobby fails for non-existent lobby`() {
        val player = createUser("Henry")
        val result =
            service.leaveLobby(123, player)
                .let {
                    check(it is Failure)
                    it.value
                }
        assertTrue(result is LobbyError.NotFound)
    }

    @Test
    fun `getAllAvailableLobbies returns all lobbies`() {
        val host = createUser("Isaac")
        service.createLobby("Lobby1", "Desc", 4, host, 2, 1)
        service.createLobby("Lobby2", "Desc", 4, host, 2, 1)

        val lobbies = service.getAllAvailableLobbies()
        assertTrue(lobbies.size >= 2)
        assertTrue(lobbies.any { it.name == "Lobby1" })
        assertTrue(lobbies.any { it.name == "Lobby2" })
    }

    @Test
    fun `deleteLobby deletes lobby when requester is host`() {
        val host = createUser("Jack")
        val lobby =
            service.createLobby("DeleteLobby", "Desc", 4, host, 2, 1).let {
                check(it is Success)
                it.value
            }

        val result =
            service.deleteLobby(lobby.id, host)
                .let {
                    check(it is Success)
                    it.value
                }
        assertTrue(result)
    }

    @Test
    fun `deleteLobby fails if requester is not host`() {
        val host = createUser("Ken")
        val lobby =
            service.createLobby("UnauthorizedLobby", "Desc", 4, host, 2, 1).let {
                check(it is Success)
                it.value
            }
        val otherUser = createUser("Leo")

        val result =
            service.deleteLobby(lobby.id, otherUser)
                .let {
                    check(it is Failure)
                    it.value
                }
        assertTrue(result is LobbyError.NotHost)
    }

    @Test
    fun `deleteLobby fails if lobby does not exist`() {
        val user = createUser("Mia")
        val result =
            service.deleteLobby(999, user)
                .let {
                    check(it is Failure)
                    it.value
                }
        assertTrue(result is LobbyError.NotFound)
    }
}
