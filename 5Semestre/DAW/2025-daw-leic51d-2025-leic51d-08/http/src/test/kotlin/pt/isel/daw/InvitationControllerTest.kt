package pt.isel.daw

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.isel.daw.http.InvitationController
import pt.isel.daw.http.model.CodeOutput
import pt.isel.daw.http.model.ValidCodeOutput
import pt.isel.daw.repo.TransactionManager
import pt.isel.daw.service.InvitationServices
import pt.isel.daw.service.Success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@SpringJUnitConfig(TestConfig::class)
class InvitationControllerTest {
    @Autowired
    private lateinit var invitationController: InvitationController

    @Autowired
    private lateinit var invitationServices: InvitationServices

    @Autowired
    private lateinit var trxManager: TransactionManager

    private lateinit var authUser: AuthenticatedUser
    private lateinit var validCode: String

    @BeforeEach
    fun setup() {
        trxManager.run {
            val superUser =
                requireNotNull(repoUsers.getById(1)) {
                    "SuperUser with id=1 must exist for tests"
                }
            authUser = AuthenticatedUser(superUser, "token")
        }

        validCode =
            invitationServices.createInvitation(createdBy = authUser.user.id).let {
                check(it is Success)
                it.value
            }
    }

    @Test
    fun `createInvitation returns CREATED and code`() {
        val response = invitationController.createInvitation(authUser)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val body = response.body
        assertIs<CodeOutput>(body)
        assertNotNull(body.code)
        assert(body.code.isNotBlank())
    }

    @Test
    fun `isValid returns OK for valid invitation`() {
        val response = invitationController.isValid(validCode)
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertIs<ValidCodeOutput>(body)
        assertEquals(true, body.valid)
    }

    @Test
    fun `isValid returns NOT_FOUND for non-existent invitation`() {
        val response = invitationController.isValid("nonexistentcode")
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `isValid returns BAD_REQUEST for already used invitation`() {
        // consume the invitation first
        trxManager.run { repoInvitations.consume(validCode) }

        val response = invitationController.isValid(validCode)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}
