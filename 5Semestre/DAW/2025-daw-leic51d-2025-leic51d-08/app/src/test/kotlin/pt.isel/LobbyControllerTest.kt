package pt.isel

import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.daw.AppPokerDice
import pt.isel.daw.UserController
import pt.isel.daw.http.model.UserCreateTokenInputModel
import pt.isel.daw.http.model.UserCreateTokenOutputModel
import pt.isel.daw.http.model.UserInput
import pt.isel.daw.repo.TransactionManager
import pt.isel.daw.service.InvitationServices
import pt.isel.daw.service.Success
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [AppPokerDice::class, TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class LobbyControllerTest {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var trxManager: TransactionManager

    @Autowired
    private lateinit var userController: UserController

    @Autowired
    private lateinit var invitationServices: InvitationServices

    private lateinit var client: WebTestClient

    private val johnDoe = UserInput("JohnDoe", "john.doe@example.com", "passworD@123", "someInviteCode")
    private val rose = UserInput("RoseMary", "rose@example.com", "rainhaDoCaisSodre@123", "someInviteCode")

    private lateinit var johnToken: String
    private lateinit var roseToken: String
    private var johnId: Int = 0
    private var roseId: Int = 0

    @BeforeTest
    fun setup() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        trxManager.run {
            repoLobbies.clear()
            repoMatches.clear()
            repoUsers.getAll().filter { it.id != 1 }.forEach { repoUsers.deleteById(it.id) }
        }

        val johnInvite =
            invitationServices.createInvitation(1).let {
                check(it is Success)
                it.value
            }
        userController.createUser(johnDoe.copy(invite_code = johnInvite)).let { resp ->
            assert(resp.statusCode.is2xxSuccessful)
            val location = requireNotNull(resp.headers.getFirst("Location"))
            johnId = location.split("/").last().toInt()
        }
        userController.token(UserCreateTokenInputModel(johnDoe.email, johnDoe.password)).let { resp ->
            assert(resp.statusCode.is2xxSuccessful)
            val body = resp.body
            assertIs<UserCreateTokenOutputModel>(body)
            johnToken = body.token
        }

        val roseInvite =
            invitationServices.createInvitation(1).let {
                check(it is Success)
                it.value
            }
        userController.createUser(rose.copy(invite_code = roseInvite)).let { resp ->
            assert(resp.statusCode.is2xxSuccessful)
            val location = requireNotNull(resp.headers.getFirst("Location"))
            roseId = location.split("/").last().toInt()
        }
        userController.token(UserCreateTokenInputModel(rose.email, rose.password)).let { resp ->
            assert(resp.statusCode.is2xxSuccessful)
            val body = resp.body
            assertIs<UserCreateTokenOutputModel>(body)
            roseToken = body.token
        }
    }

    @Test
    fun `can create a lobby successfully`() {
        val input =
            mapOf(
                "name" to "FunLobby",
                "description" to "A fun lobby",
                "max_players" to 4,
                "rounds" to 10,
                "ante" to 100,
            )
        client
            .post()
            .uri("/lobbies")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(input)
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .value("Location") { assertTrue(it.startsWith("/api/lobbies/")) }
    }

    @Test
    fun `cannot create a lobby with duplicate name`() {
        val input =
            mapOf(
                "name" to "DuplicateLobby",
                "description" to "DuplicateLobby",
                "max_players" to 4,
                "rounds" to 10,
                "ante" to 100,
            )
        client
            .post()
            .uri("/lobbies")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(input)
            .exchange()
            .expectStatus()
            .isCreated

        client
            .post()
            .uri("/lobbies")
            .header("Authorization", "Bearer $roseToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(input)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("title")
            .isEqualTo("lobby-name-already-in-use")
    }

    @Test
    fun `can join a lobby`() {
        val input =
            mapOf(
                "name" to "JoinableLobby",
                "description" to "Join test",
                "max_players" to 4,
                "rounds" to 10,
                "ante" to 100,
            )
        val location =
            client
                .post()
                .uri("/lobbies")
                .header("Authorization", "Bearer $johnToken")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)
                .exchange()
                .returnResult(Unit::class.java)
                .responseHeaders["Location"]
                ?.first()
        requireNotNull(location)
        val lobbyId = location.split("/").last()

        client
            .post()
            .uri("/lobbies/$lobbyId/join")
            .header("Authorization", "Bearer $roseToken")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .value("Location") { assertTrue(it.endsWith("/$lobbyId")) }
    }

    @Test
    fun `cannot join nonexistent lobby`() {
        client
            .post()
            .uri("/lobbies/999/join")
            .header("Authorization", "Bearer $roseToken")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody()
            .jsonPath("title")
            .isEqualTo("lobby-not-found")
    }

    @Test
    fun `can leave a lobby`() {
        val input =
            mapOf(
                "name" to "LeaveLobby",
                "description" to "Leave test",
                "max_players" to 4,
                "rounds" to 10,
                "ante" to 100,
            )
        val location =
            client
                .post()
                .uri("/lobbies")
                .header("Authorization", "Bearer $johnToken")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)
                .exchange()
                .returnResult(Unit::class.java)
                .responseHeaders["Location"]
                ?.first()
        requireNotNull(location)
        val lobbyId = location.split("/").last()

        client
            .post()
            .uri("/lobbies/$lobbyId/join")
            .header("Authorization", "Bearer $roseToken")
            .exchange()
            .expectStatus()
            .isOk

        client
            .post()
            .uri("/lobbies/$lobbyId/leave")
            .header("Authorization", "Bearer $roseToken")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .value("Location") { assertTrue(it.endsWith("/lobbies")) }
    }

    @Test
    fun `cannot leave nonexistent lobby`() {
        client
            .post()
            .uri("/lobbies/999/leave")
            .header("Authorization", "Bearer $roseToken")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody()
            .jsonPath("title")
            .isEqualTo("lobby-not-found")
    }

    @Test
    fun `can delete lobby as host`() {
        val input =
            mapOf(
                "name" to "DeleteLobby",
                "description" to "Delete test",
                "max_players" to 4,
                "rounds" to 10,
                "ante" to 100,
            )
        val location =
            client
                .post()
                .uri("/lobbies")
                .header("Authorization", "Bearer $johnToken")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)
                .exchange()
                .returnResult(Unit::class.java)
                .responseHeaders["Location"]
                ?.first()
        requireNotNull(location)
        val lobbyId = location.split("/").last()

        client
            .post()
            .uri("/lobbies/$lobbyId/delete")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .value("Location") { assertTrue(it.endsWith("/lobbies")) }
    }

    @Test
    fun `cannot delete lobby if not host`() {
        val input =
            mapOf(
                "name" to "NotHostDelete",
                "description" to "test",
                "max_players" to 4,
                "rounds" to 10,
                "ante" to 100,
            )
        val location =
            client
                .post()
                .uri("/lobbies")
                .header("Authorization", "Bearer $johnToken")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)
                .exchange()
                .returnResult(Unit::class.java)
                .responseHeaders["Location"]
                ?.first()
        requireNotNull(location)
        val lobbyId = location.split("/").last()

        client
            .post()
            .uri("/lobbies/$lobbyId/delete")
            .header("Authorization", "Bearer $roseToken")
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody()
            .jsonPath("title")
            .isEqualTo("not-host")
    }

    @Test
    fun `can get all lobbies`() {
        val input1 =
            mapOf(
                "name" to "All1",
                "description" to "First",
                "max_players" to 4,
                "rounds" to 5,
                "ante" to 50,
            )

        val input2 =
            mapOf(
                "name" to "All2",
                "description" to "Second",
                "max_players" to 4,
                "rounds" to 5,
                "ante" to 50,
            )

        client
            .post()
            .uri("/lobbies")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(input1)
            .exchange()
            .expectStatus()
            .isCreated

        client
            .post()
            .uri("/lobbies")
            .header("Authorization", "Bearer $roseToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(input2)
            .exchange()
            .expectStatus()
            .isCreated

        client
            .get()
            .uri("/lobbies/all")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$[0].name")
            .isEqualTo("All1")
            .jsonPath("$[1].name")
            .isEqualTo("All2")
    }
}
