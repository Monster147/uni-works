package pt.isel.daw

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.isel.daw.http.LobbyController
import pt.isel.daw.http.model.LobbyInput
import pt.isel.daw.http.model.LobbyOutput
import pt.isel.daw.repo.TransactionManager
import pt.isel.daw.service.InvitationServices
import pt.isel.daw.service.Success
import pt.isel.daw.service.UserServices
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class LobbyControllerTest {
    @Autowired
    private lateinit var lobbyController: LobbyController

    @Autowired
    private lateinit var trxManager: TransactionManager

    @Autowired
    private lateinit var userServices: UserServices

    @Autowired
    private lateinit var invitationServices: InvitationServices

    private lateinit var invitation: String

    private lateinit var host: User
    private lateinit var player2: User
    private lateinit var hostToken: String
    private lateinit var player2Token: String

    @BeforeEach
    fun cleanup() {
        trxManager.run {
            repoLobbies.clear()
            repoMatches.clear()
            repoUsers.getAll().filter { it.id != 1 }.forEach { repoUsers.deleteById(it.id) }
        }

        val hostInvite =
            invitationServices.createInvitation(1).let {
                check(it is Success)
                it.value
            }

        host =
            userServices.createUser("Host", "host@mail.com", "Pass@123", hostInvite).let {
                check(it is Success)
                it.value
            }

        val player2Invite =
            invitationServices.createInvitation(1).let {
                check(it is Success)
                it.value
            }

        player2 =
            userServices.createUser("Player2", "p2@mail.com", "Pass@123", player2Invite).let {
                check(it is Success)
                it.value
            }

        hostToken =
            userServices.createToken("host@mail.com", "Pass@123").let {
                check(it is Success)
                it.value.tokenValue
            }

        player2Token =
            userServices.createToken("p2@mail.com", "Pass@123").let {
                check(it is Success)
                it.value.tokenValue
            }
    }

    @Test
    fun `host can create lobby`() {
        val auth = AuthenticatedUser(host, hostToken)
        val input = LobbyInput("Lobby1", "Fun Game", 4, 3, 10)

        val response = lobbyController.createLobby(auth, input)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.getFirst(HttpHeaders.LOCATION)
        assertNotNull(location)
        assertTrue(location.startsWith("/api/lobbies/"))
    }

    @Test
    fun `cannot create lobby with duplicate name`() {
        val auth = AuthenticatedUser(host, hostToken)
        val input = LobbyInput("SameName", "Desc", 4, 3, 10)
        lobbyController.createLobby(auth, input)
        val resp = lobbyController.createLobby(auth, input)
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
    }

    @Test
    fun `player can join existing lobby`() {
        val auth = AuthenticatedUser(host, hostToken)
        val input = LobbyInput("LobbyJoin", "Desc", 4, 3, 10)
        lobbyController.createLobby(auth, input)

        val lobbyId = trxManager.run { repoLobbies.getAll().first().id }

        val joinAuth = AuthenticatedUser(player2, player2Token)
        val resp = lobbyController.joinLobby(joinAuth, lobbyId)
        assertEquals(HttpStatus.OK, resp.statusCode)
        assertTrue(resp.headers.getFirst(HttpHeaders.LOCATION)?.contains("/api/lobbies/$lobbyId") == true)
    }

    @Test
    fun `joinLobby returns NOT_FOUND if lobby doesn't exist`() {
        val joinAuth = AuthenticatedUser(player2, player2Token)
        val resp = lobbyController.joinLobby(joinAuth, 999)
        assertEquals(HttpStatus.NOT_FOUND, resp.statusCode)
    }

    @Test
    fun `player can leave lobby`() {
        val auth = AuthenticatedUser(host, hostToken)
        val input = LobbyInput("LobbyLeave", "Desc", 4, 3, 10)
        lobbyController.createLobby(auth, input)
        val lobbyId = trxManager.run { repoLobbies.getAll().first().id }

        val joinAuth = AuthenticatedUser(player2, player2Token)
        lobbyController.joinLobby(joinAuth, lobbyId)

        val leaveResp = lobbyController.leaveLobby(joinAuth, lobbyId)
        assertEquals(HttpStatus.OK, leaveResp.statusCode)
        assertEquals("/api/lobbies", leaveResp.headers.getFirst(HttpHeaders.LOCATION))
    }

    @Test
    fun `fails to leave a non-existent lobby`() {
        val authHost = AuthenticatedUser(host, hostToken)
        val response = lobbyController.leaveLobby(authHost, 42)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `getAllAvailableLobbies returns created lobbies`() {
        val auth = AuthenticatedUser(host, hostToken)
        val input1 = LobbyInput("LobbyA", "Desc", 4, 3, 10)
        val input2 = LobbyInput("LobbyB", "Desc", 4, 3, 10)
        lobbyController.createLobby(auth, input1)
        lobbyController.createLobby(auth, input2)

        val resp = lobbyController.getAllAvailableLobbies()
        assertEquals(HttpStatus.OK, resp.statusCode)

        val body = resp.body as List<*>
        assertTrue(body.size >= 2)
        assertIs<LobbyOutput>(body.first())
    }

    @Test
    fun `host can delete lobby`() {
        val auth = AuthenticatedUser(host, hostToken)
        val input = LobbyInput("LobbyDelete", "Desc", 4, 3, 10)
        lobbyController.createLobby(auth, input)
        val lobbyId = trxManager.run { repoLobbies.getAll().first().id }

        val resp = lobbyController.deleteLobby(auth, lobbyId)
        assertEquals(HttpStatus.OK, resp.statusCode)
        assertEquals("/api/lobbies", resp.headers.getFirst(HttpHeaders.LOCATION))
    }

    @Test
    fun `non-host cannot delete lobby`() {
        val auth = AuthenticatedUser(host, hostToken)
        val input = LobbyInput("LobbyNoDelete", "Desc", 4, 3, 10)
        lobbyController.createLobby(auth, input)
        val lobbyId = trxManager.run { repoLobbies.getAll().first().id }

        val nonHost = AuthenticatedUser(player2, player2Token)
        val resp = lobbyController.deleteLobby(nonHost, lobbyId)
        assertEquals(HttpStatus.FORBIDDEN, resp.statusCode)
    }

    @Test
    fun `fails to delete lobby if not found`() {
        val authHost = AuthenticatedUser(host, hostToken)
        val response = lobbyController.deleteLobby(authHost, 999)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
