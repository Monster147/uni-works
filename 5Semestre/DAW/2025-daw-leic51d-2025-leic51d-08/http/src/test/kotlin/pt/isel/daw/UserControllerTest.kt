package pt.isel.daw

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.isel.daw.http.model.Problem
import pt.isel.daw.http.model.UserCreateTokenInputModel
import pt.isel.daw.http.model.UserCreateTokenOutputModel
import pt.isel.daw.http.model.UserHomeOutputModel
import pt.isel.daw.http.model.UserInput
import pt.isel.daw.repo.TransactionManager
import pt.isel.daw.service.InvitationServices
import pt.isel.daw.service.Success
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class UserControllerTest {
    @Autowired
    private lateinit var controllerUser: UserController

    @Autowired
    private lateinit var trxManager: TransactionManager

    @Autowired
    private lateinit var invitationServices: InvitationServices

    private lateinit var invitation: String

    @BeforeEach
    fun cleanup() {
        trxManager.run {
            repoLobbies.clear()
            repoMatches.clear()
            repoUsers.getAll().filter { it.id != 1 }.forEach { repoUsers.deleteById(it.id) }
        }

        invitation =
            invitationServices.createInvitation(1).let {
                check(it is Success)
                it.value
            }
    }

    @Test
    fun `can create an user, obtain a token, and access user home, and logout`() {
        // given: a user
        val name = "JohnRambo"
        val email = "john@rambo.vcom"
        val password = "badGuy@123"
        val balance = 500

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        val userId =
            controllerUser.createUser(UserInput(name, email, password, invitation)).let { resp ->
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                val location = resp.headers.getFirst(HttpHeaders.LOCATION)
                assertNotNull(location)
                assertTrue(location.startsWith("/api/users"))
                location.split("/").last().toInt()
            }

        // when: creating a token
        // then: the response is a 200
        val token =
            controllerUser.token(UserCreateTokenInputModel(email, password)).let { resp ->
                assertEquals(HttpStatus.OK, resp.statusCode)
                assertIs<UserCreateTokenOutputModel>(resp.body)
                (resp.body as UserCreateTokenOutputModel).token
            }

        // when: getting the user home with a valid token
        // then: the response is a 200 with the proper representation
        val user = User(userId, name, email, PasswordValidationInfo(password))
        controllerUser.userHome(AuthenticatedUser(user, token)).also { resp ->
            assertEquals(HttpStatus.OK, resp.statusCode)
            assertEquals(UserHomeOutputModel(userId, name, email, balance), resp.body)
        }
    }

    @Test
    fun `create user fails if email already in use`() {
        val email = "used@mail.com"
        val password = "GoodPass1!"
        val name = "User1"

        // Create first user successfully
        controllerUser.createUser(UserInput(name, email, password, invitation))

        // Try to create another user with same email
        val response = controllerUser.createUser(UserInput("Another", email, password, invitation))
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(Problem.EmailAlreadyInUse, response.body)
    }

    @Test
    fun `create user fails if password is insecure`() {
        val response = controllerUser.createUser(UserInput("John", "john@mail.com", "123", invitation))
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(Problem.InsecurePassword, response.body)
    }

    @Test
    fun `token fails if email does not exist`() {
        val response = controllerUser.token(UserCreateTokenInputModel("nonexistent@mail.com", "AnyPass1!"))
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(Problem.UserOrPasswordAreInvalid, response.body)
    }

    @Test
    fun `token fails if password is wrong`() {
        val email = "user@mail.com"
        val password = "GoodPass1!"

        // Create user
        controllerUser.createUser(UserInput("John", email, password, invitation))

        // Try token with wrong password
        val response = controllerUser.token(UserCreateTokenInputModel(email, "WrongPass1!"))
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(Problem.UserOrPasswordAreInvalid, response.body)
    }

    @Test
    fun `logout revokes token`() {
        val email = "logout@mail.com"
        val password = "Secure1!"
        val userIdLocation =
            controllerUser.createUser(
                UserInput("John", email, password, invitation),
            ).headers.getFirst(HttpHeaders.LOCATION)
        val userId = userIdLocation?.split("/")?.last()?.toInt()
        requireNotNull(userId)

        val tokenModel =
            controllerUser.token(UserCreateTokenInputModel(email, password)).body as UserCreateTokenOutputModel
        val authUser =
            AuthenticatedUser(User(userId, "John", email, PasswordValidationInfo(password)), tokenModel.token)

        controllerUser.logout(authUser)

        val retrievedUser = controllerUser.testUserServices.getUserByToken(tokenModel.token)
        assertEquals(null, retrievedUser)
    }

    @Test
    fun `userHome returns correct user info`() {
        val email = "home@mail.com"
        val password = "SafePass1!"
        val name = "HomeUser"
        val balance = 50

        val userIdLocation =
            controllerUser.createUser(
                UserInput(
                    name,
                    email,
                    password,
                    invitation,
                ),
            ).headers.getFirst(HttpHeaders.LOCATION)
        val userId = userIdLocation?.split("/")?.last()?.toInt()
        requireNotNull(userId)

        val token = controllerUser.token(UserCreateTokenInputModel(email, password)).body as UserCreateTokenOutputModel
        val authUser =
            AuthenticatedUser(
                User(userId, name, email, PasswordValidationInfo(password), balance = balance),
                token.token,
            )

        val response = controllerUser.userHome(authUser)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(UserHomeOutputModel(userId, name, email, balance), response.body)
    }
}
