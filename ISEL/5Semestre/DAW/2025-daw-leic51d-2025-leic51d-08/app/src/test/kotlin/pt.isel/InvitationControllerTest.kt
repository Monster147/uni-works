package pt.isel

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.daw.AppPokerDice
import pt.isel.daw.http.model.CodeOutput
import pt.isel.daw.http.model.ValidCodeOutput
import pt.isel.daw.repo.TransactionManager
import pt.isel.daw.service.InvitationServices
import pt.isel.daw.service.Success
import pt.isel.daw.service.UserServices
import kotlin.test.Test
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [AppPokerDice::class, TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class InvitationControllerTest {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var trxManager: TransactionManager

    @Autowired
    private lateinit var invitationServices: InvitationServices

    @Autowired
    private lateinit var userServices: UserServices

    private lateinit var client: WebTestClient

    private lateinit var superUserEmail: String
    private lateinit var superUserToken: String

    @BeforeAll
    fun setup() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        trxManager.run {
            repoLobbies.clear()
            repoMatches.clear()
            repoUsers.getAll().filter { it.id != 1 }.forEach { repoUsers.deleteById(it.id) }
            superUserEmail =
                repoUsers.getById(1).let {
                    requireNotNull(it).email
                }
        }

        val password = "adminPassword123"
        superUserToken =
            userServices.createToken(superUserEmail, password).let {
                check(it is Success)
                it.value.tokenValue
            }
    }

    @Test
    fun `can create an invitation`() {
        val result =
            client
                .post()
                .uri("/invitations")
                .header("Authorization", "Bearer $superUserToken")
                .exchange()
                .expectStatus()
                .isCreated
                .expectBody(CodeOutput::class.java)
                .returnResult()
                .responseBody!!

        assertTrue(result.code.isNotEmpty())
    }

    @Test
    fun `can validate an invitation code`() {
        // First create a valid invitation
        val code =
            invitationServices.createInvitation(1).let {
                check(it is Success)
                it.value
            }

        client
            .get()
            .uri("/invitations/$code")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ValidCodeOutput::class.java)
            .returnResult()
            .responseBody!!
            .also {
                assertTrue(it.valid)
            }
    }

    @Test
    fun `invalid invitation returns 404`() {
        client
            .get()
            .uri("/invitations/nonexistent-code")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody()
            .jsonPath("title")
            .isEqualTo("invalid-invitation")
    }

    @Test
    fun `used invitation returns 400`() {
        val code =
            invitationServices.createInvitation(1).let {
                check(it is Success)
                it.value
            }

        val newUser =
            mapOf(
                "name" to "UsedUser",
                "email" to "used@example.com",
                "password" to "Password@123",
                "invite_code" to code,
            )

        client
            .post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(newUser)
            .exchange()
            .expectStatus()
            .isCreated

        client
            .get()
            .uri("/invitations/$code")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("title")
            .isEqualTo("invitation-already-used")
    }
}
