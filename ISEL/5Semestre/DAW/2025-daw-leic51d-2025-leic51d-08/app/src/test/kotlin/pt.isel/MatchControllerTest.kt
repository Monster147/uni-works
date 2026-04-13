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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [AppPokerDice::class, TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class MatchControllerTest {
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

    private var johnId: Int = 0
    private var roseId: Int = 0
    private lateinit var johnToken: String
    private lateinit var roseToken: String

    @BeforeTest
    fun setup() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

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

        val lobbyInput =
            mapOf(
                "name" to "TestLobby",
                "description" to "Test Lobby for Matches",
                "max_players" to 2,
                "rounds" to 5,
                "ante" to 5,
            )

        client
            .post()
            .uri("/api/lobbies")
            .header("Authorization", "Bearer $johnToken")
            .bodyValue(lobbyInput)
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .exists("Location")

        client
            .post()
            .uri("/api/lobbies/1/join")
            .header("Authorization", "Bearer $roseToken")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Location")
    }

    @Test
    fun `start match successfully`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .exists("Location")
    }

    @Test
    fun `start match forbidden if not host`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $roseToken")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `start next round success`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isCreated

        client
            .post()
            .uri("/api/matches/1/rounds/start")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Location")
    }

    @Test
    fun `play turn valid`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isCreated

        client
            .post()
            .uri("/api/matches/1/rounds/start")
            .exchange()
            .expectStatus()
            .isOk

        client
            .post()
            .uri("/api/matches/1/turns/play")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Location")
    }

    @Test
    fun `player can reroll dice after first roll`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isCreated

        client
            .post()
            .uri("/api/matches/1/rounds/start")
            .exchange()
            .expectStatus()
            .isOk

        client
            .post()
            .uri("/api/matches/1/turns/play")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Location")

        client
            .post()
            .uri("/api/matches/1/turns/play")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"kept_dice":[true,false,true,false,true]}""") // Keep dice 1,3,5
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Location")
    }

    @Test
    fun `player cannot reroll more than allowed times`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isCreated

        client
            .post()
            .uri("/api/matches/1/rounds/start")
            .exchange()
            .expectStatus()
            .isOk

        client
            .post()
            .uri("/api/matches/1/turns/play")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Location")

        repeat(2) {
            client
                .post()
                .uri("/api/matches/1/turns/play")
                .header("Authorization", "Bearer $johnToken")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""{"kept_dice":[true,false,true,false,true]}""")
                .exchange()
        }

        client
            .post()
            .uri("/api/matches/1/turns/play")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"kept_dice":[true,true,true,true,true]}""")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `play turn invalid dice length`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isCreated
        client
            .post()
            .uri("/api/matches/1/rounds/start")
            .exchange()
            .expectStatus()
            .isOk

        client
            .post()
            .uri("/api/matches/1/turns/play")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Location")

        client
            .post()
            .uri("/api/matches/1/turns/play")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"kept_dice":[true,true,true,true]}""")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `play turn out of turn`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
        client.post().uri("/api/matches/1/rounds/start").exchange()

        client
            .post()
            .uri("/api/matches/1/turns/play")
            .header("Authorization", "Bearer $roseToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `pass turn success`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
        client.post().uri("/api/matches/1/rounds/start").exchange()

        client
            .post()
            .uri("/api/matches/1/turns/play")
            .header("Authorization", "Bearer $johnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Location")

        client
            .post()
            .uri("/api/matches/1/turns/pass")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `finish round success`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
        client.post().uri("/api/matches/1/rounds/start").exchange()

        listOf(johnToken, roseToken).forEach { token ->
            client
                .post()
                .uri("/api/matches/1/turns/play")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .exists("Location")

            client
                .post()
                .uri("/api/matches/1/turns/pass")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus()
                .isOk
        }

        client
            .post()
            .uri("/api/matches/1/finish")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Location")
    }

    @Test
    fun `get match and round`() {
        client
            .post()
            .uri("/api/lobbies/1/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
        client.post().uri("/api/matches/1/rounds/start").exchange()

        client
            .get()
            .uri("/api/matches/1")
            .exchange()
            .expectStatus()
            .isOk

        client
            .get()
            .uri("/api/matches/1/rounds/1")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `returns 404 if lobby does not exist`() {
        client
            .post()
            .uri("/api/lobbies/999/start")
            .header("Authorization", "Bearer $johnToken")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
