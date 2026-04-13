package pt.isel.daw

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.isel.daw.repo.RepositoryUser
import pt.isel.daw.repo.mem.RepositoryUserInMem
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RepositoryMemUsersTest {
    private lateinit var repo: RepositoryUser

    @BeforeEach
    fun setup() {
        repo = RepositoryUserInMem()
    }

    @Test
    fun `createUser and findById`() {
        val user = repo.createUser("Alice", "alice@isel.pt", PasswordValidationInfo("hash"))
        val found = repo.getById(user.id)
        assertEquals(user, found)
    }

    @Test
    fun `findByEmail returns correct user`() {
        val user = repo.createUser("Bob", "bob@isel.pt", PasswordValidationInfo("hash2"))
        val found = repo.findByEmail("bob@isel.pt")
        assertEquals(user, found)
        assertNull(repo.findByEmail("notfound@isel.pt"))
    }

    @Test
    fun `getAll returns all users`() {
        val user = repo.createUser("Alice", "alice@isel.pt", PasswordValidationInfo("hash"))
        val user2 = repo.createUser("Bob", "bob@isel.pt", PasswordValidationInfo("hash2"))
        val superUser = repo.getById(1) // superUser has id 1
        val superUser2 = repo.getById(2)
        val usersFound = repo.getAll()
        assertEquals(4, usersFound.size)
        assertEquals(listOf(superUser, superUser2, user, user2), usersFound)
    }

    @Test
    fun `createUser and update its name and email and check changes`() {
        val user = repo.createUser("Alice", "alice@isel.pt", PasswordValidationInfo("hash"))
        val found = repo.getById(user.id)
        assertEquals(user, found)
        val updatedUser = user.copy(name = "AliceUpdated", email = "updated@land.com")
        repo.save(updatedUser)
        val foundUpdated = repo.getById(user.id)
        assertEquals(updatedUser, foundUpdated)
    }

    @Test
    fun `deleteById removes the user`() {
        val user = repo.createUser("Abilio", "abilio@hotmail.com", PasswordValidationInfo("hash3"))
        val deleted = repo.deleteById(user.id)
        val shouldBeNull = repo.getById(user.id)
        assertEquals(true, deleted)
        assertNull(shouldBeNull)
    }

    @Test
    fun `createToken and getTokenByTokenValidationInfo`() {
        val user = repo.createUser("Carol", "carol@isel.pt", PasswordValidationInfo("hash3"))
        val tokenValidationInfo = TokenValidationInfo("token123")
        val now = Instant.now()
        val token = Token(tokenValidationInfo, user.id, now, now)
        repo.createToken(token, maxTokens = 2)
        val result = repo.getTokenByTokenValidationInfo(tokenValidationInfo)
        assertNotNull(result)
        assertEquals(user, result.first)
        assertEquals(token, result.second)
    }

    @Test
    fun `createToken removes oldest when maxTokens exceeded`() {
        val user = repo.createUser("Dave", "dave@isel.pt", PasswordValidationInfo("hash4"))
        val init = Instant.now().minusSeconds(60)
        val t1 = Token(TokenValidationInfo("t1"), user.id, init, Instant.now().minusSeconds(10))
        val t2 = Token(TokenValidationInfo("t2"), user.id, init, Instant.now().minusSeconds(5))
        val t3 = Token(TokenValidationInfo("t3"), user.id, init, Instant.now())
        repo.createToken(t1, maxTokens = 2)
        repo.createToken(t2, maxTokens = 2)
        repo.createToken(t3, maxTokens = 2)
        // t1 should be removed
        assertNull(repo.getTokenByTokenValidationInfo(TokenValidationInfo("t1")))
        assertNotNull(repo.getTokenByTokenValidationInfo(TokenValidationInfo("t2")))
        assertNotNull(repo.getTokenByTokenValidationInfo(TokenValidationInfo("t3")))
    }

    @Test
    fun `updateTokenLastUsed replaces token`() {
        val user = repo.createUser("Eve", "eve@isel.pt", PasswordValidationInfo("hash5"))
        val info = TokenValidationInfo("tokenEve")
        val init = Instant.now().minusSeconds(200)
        val tokenOld = Token(info, user.id, init, Instant.now().minusSeconds(100))
        repo.createToken(tokenOld, maxTokens = 2)
        val tokenNew = Token(info, user.id, init, Instant.now())
        repo.updateTokenLastUsed(tokenNew, tokenNew.lastUsedAt)
        val result = repo.getTokenByTokenValidationInfo(info)
        assertNotNull(result)
        assertEquals(tokenNew, result.second)
    }

    @Test
    fun `removeTokenByValidationInfo removes token`() {
        val user = repo.createUser("Frank", "frank@isel.pt", PasswordValidationInfo("hash6"))
        val info = TokenValidationInfo("tokenFrank")
        val token = Token(info, user.id, Instant.now(), Instant.now())
        repo.createToken(token, maxTokens = 2)
        val removed = repo.removeTokenByValidationInfo(info)
        assertEquals(1, removed)
        assertNull(repo.getTokenByTokenValidationInfo(info))
    }
}
