package pt.isel.daw.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class TestUserServices {
    @Autowired
    private lateinit var service: UserServices

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var validInvite: String

    @Autowired
    private lateinit var invitationServices: InvitationServices

    @BeforeEach
    fun setup() {
        // criar um invite válido antes de cada teste
        validInvite =
            invitationServices.createInvitation(1).let {
                check(it is Success)
                it.value
            }
    }

    @Test
    fun `createUser stores user and encodes password`() {
        val user =
            service.createUser("Alice", "alice@isel.pt", "passworD123@", validInvite).let {
                check(it is Success)
                it.value
            }
        assertEquals("Alice", user.name)
        assertEquals("alice@isel.pt", user.email)
        assertTrue(passwordEncoder.matches("passworD123@", user.passwordValidation.validationInfo))
    }

    @Test
    fun `createToken returns token for valid credentials`() {
        service.createUser("Bob", "bob@isel.pt", "secreT@123", validInvite)
        val tokenInfo =
            service.createToken("bob@isel.pt", "secreT@123").let {
                check(it is Success)
                it.value
            }
        assertTrue(tokenInfo.tokenValue.isNotEmpty())
        assertTrue(tokenInfo.tokenExpiration.isAfter(Instant.now()))
    }

    @Test
    fun `getUserByToken returns user for valid token`() {
        val user =
            service.createUser("Carol", "carol@isel.pt", "pasS@1234", validInvite).let {
                check(it is Success)
                it.value
            }
        val tokenInfo =
            service.createToken("carol@isel.pt", "pasS@1234").let {
                check(it is Success)
                it.value
            }
        val found = service.getUserByToken(tokenInfo.tokenValue)
        assertNotNull(found)
        assertEquals(user.id, found.id)
    }

    @Test
    fun `revokeToken removes token`() {
        service.createUser("Dave", "dave@isel.pt", "pW@123456", validInvite)
        val tokenInfo =
            service.createToken("dave@isel.pt", "pW@123456").let {
                check(it is Success)
                it.value
            }
        val revoked = service.revokeToken(tokenInfo.tokenValue)
        assertTrue(revoked)
        val found = service.getUserByToken(tokenInfo.tokenValue)
        assertNull(found)
    }

    @Test
    fun `createToken throws for invalid password`() {
        service.createUser("Eve", "eve@isel.pt", "pw1", validInvite)
        service.createToken("eve@isel.pt", "wrongpw").also {
            assertTrue(it is Either.Left<*>)
            assertTrue(it.value is TokenCreationError.UserOrPasswordAreInvalid)
        }
    }

    @Test
    fun `createToken throws for non-existent email`() {
        service.createToken("notfound@isel.pt", "pw").also {
            assertTrue(it is Either.Left<*>)
            assertTrue(it.value is TokenCreationError.UserOrPasswordAreInvalid)
        }
    }

    @Test
    fun `getUserByToken returns null for invalid token`() {
        val result = service.getUserByToken("invalidtoken")
        assertNull(result)
    }
}
