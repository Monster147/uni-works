package pt.isel

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.daw.AppPokerDice
import pt.isel.daw.PasswordValidationInfo
import pt.isel.daw.http.model.UserCreateTokenOutputModel
import pt.isel.daw.http.model.UserInput
import pt.isel.daw.repo.TransactionManager
import pt.isel.daw.service.InvitationServices
import pt.isel.daw.service.Success
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertTrue

fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [AppPokerDice::class, TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class UserControllerTest {
    // Injected by the test environment
    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var trxManager: TransactionManager

    @Autowired
    private lateinit var invitationServices: InvitationServices

    private lateinit var inviteCode: String

    val johnDoe =
        UserInput(
            name = "JohnDoe",
            email = "john.doe@example.com",
            password = "passworD@123",
            invite_code = "someInviteCode",
        )

    @BeforeAll
    fun setup() {
        trxManager.run {
            repoLobbies.clear()
            repoMatches.clear()
            repoUsers.getAll().filter { it.id != 1 }.forEach { repoUsers.deleteById(it.id) }

            inviteCode =
                invitationServices.createInvitation(1).let {
                    check(it is Success)
                    it.value
                }

            repoUsers.createUser(
                johnDoe.name,
                johnDoe.email,
                PasswordValidationInfo(newTokenValidationData()),
            )
        }
    }

    @Test
    fun `can create an user, obtain a token, and access user home, and logout`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a user
        val name = "JohnRambo"
        val email = "john@rambo.vcom"
        val password = "badGuy@123"

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        client
            .post()
            .uri("/users")
            .bodyValue(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password,
                    "invite_code" to inviteCode,
                ),
            ).exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .value("location") {
                assertTrue(it.startsWith("/api/users/"))
            }

        // when: creating a token
        // then: the response is a 200
        val result =
            client
                .post()
                .uri("/users/token")
                .bodyValue(
                    mapOf(
                        "email" to email,
                        "password" to password,
                    ),
                ).exchange()
                .expectStatus()
                .isOk
                .expectBody(UserCreateTokenOutputModel::class.java)
                .returnResult()
                .responseBody!!

        // when: getting the user home with a valid token
        // then: the response is a 200 with the proper representation
        client
            .get()
            .uri("/me")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("email")
            .isEqualTo(email)
            .jsonPath("name")
            .isEqualTo(name)

        // when: getting the user home with an invalid token
        // then: the response is a 401 with the proper problem
        client
            .get()
            .uri("/me")
            .header("Authorization", "Bearer ${result.token}-invalid")
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .valueEquals("WWW-Authenticate", "bearer")

        // when: revoking the token
        // then: response is a 200
        client
            .post()
            .uri("/logout")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus()
            .isOk

        // when: getting the user home with the revoked token
        // then: response is a 401
        client
            .get()
            .uri("/me")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .valueEquals("WWW-Authenticate", "bearer")
    }

    @Test
    fun `should create a participant and return 201 status`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        val newInviteCode =
            invitationServices.createInvitation(1).let {
                check(it is Success) { "Failed to create invitation" }
                it.value
            }

        // and: a random participant
        val rose =
            mapOf(
                "name" to "RoseMary",
                "email" to "rose@example.com",
                "password" to "rainhaDoCaisSodre@123",
                "invite_code" to newInviteCode,
            ) // UserInput(name = "RoseMary", email = "rose@example.com", "rainhaDoCaisSodre@123", newInviteCode)

        // Perform the request and assert the results
        client
            .post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(rose)
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .value("location") {
                assertTrue(it.startsWith("/api/users/"))
            }
    }

    @Test
    fun `should return 409 when email is already in use`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // Perform the request and assert the results
        client
            .post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(johnDoe)
            .exchange()
            .expectStatus()
            .isEqualTo(400)
            .expectHeader()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("title")
            .isEqualTo("email-already-in-use")
    }
}
