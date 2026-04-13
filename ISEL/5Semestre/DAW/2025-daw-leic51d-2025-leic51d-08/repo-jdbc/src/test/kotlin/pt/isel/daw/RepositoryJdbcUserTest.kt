package pt.isel.daw

import org.junit.jupiter.api.Test
import pt.isel.daw.repo.jdbc.DatabaseConnection
import pt.isel.daw.repo.jdbc.TransactionManagerJdbc
import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RepositoryJdbcUserTest {
    companion object {
        val trxManager = TransactionManagerJdbc()
        val con = DatabaseConnection.getConnection()
    }

    @BeforeTest
    fun setup() {
        val sql = "TRUNCATE dbo.users RESTART IDENTITY CASCADE;"
        con.prepareStatement(sql).use { stmt ->
            stmt.executeUpdate()
        }
        con.commit()
    }

    @Test
    fun `createUser and getById`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@isel.pt", PasswordValidationInfo("hash"))
            val userGot = repoUsers.getById(user.id)
            assertEquals(user, userGot)
        }
    }

    @Test
    fun `createUser and update its name and email and check changes`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@isel.pt", PasswordValidationInfo("hash"))
            val found = repoUsers.getById(user.id)
            assertEquals(user, found)
            val updatedUser = user.copy(name = "AliceUpdated", email = "updated@land.com")
            repoUsers.save(updatedUser)
            val foundUpdated = repoUsers.getById(user.id)
            assertEquals(updatedUser, foundUpdated)
        }
    }

    @Test
    fun `getAll returns all users`() {
        trxManager.run {
            val user = repoUsers.createUser("Alice", "alice@isel.pt", PasswordValidationInfo("hash"))
            val user2 = repoUsers.createUser("Bob", "bob@example.pt", PasswordValidationInfo("hash2"))
            val user3 = repoUsers.createUser("Charlie", "charlie@email.pt", PasswordValidationInfo("hash3"))
            val allUsers = repoUsers.getAll()
            assertEquals(3, allUsers.size)
            assertEquals(listOf(user, user2, user3), allUsers)
        }
    }

    @Test
    fun `findByEmail returns correct user`() {
        trxManager.run {
            val user = repoUsers.createUser("Bob", "bob@example.com", PasswordValidationInfo("hash2"))
            val found = repoUsers.findByEmail("bob@example.com")
            assertEquals(user, found)
            assertNull(repoUsers.findByEmail("notfound@isel.pt"))
        }
    }

    @Test
    fun `deleteById removes the user`() {
        trxManager.run {
            val user = repoUsers.createUser("Abilio", "abilio@hotmail.com", PasswordValidationInfo("hash3"))
            val deleted = repoUsers.deleteById(user.id)
            val shouldBeNull = repoUsers.getById(user.id)
            assertEquals(true, deleted)
            assertNull(shouldBeNull)
        }
    }

    @Test
    fun `createToken and getTokenByTokenValidationInfo`() {
        trxManager.run {
            val user = repoUsers.createUser("Fabio", "fabio@isel.pt", PasswordValidationInfo("hash4"))
            val tokenValidationInfo = TokenValidationInfo("token123")
            val now = Instant.now().truncatedTo(SECONDS)
            val token = Token(tokenValidationInfo, user.id, now, now)
            repoUsers.createToken(token, maxTokens = 2)
            val result = repoUsers.getTokenByTokenValidationInfo(tokenValidationInfo)
            assertNotNull(result)
            assertEquals(user, result.first)
            assertEquals(token, result.second)
        }
    }

    @Test
    fun `createToken removes oldest when maxTokens exceeded`() {
        trxManager.run {
            val user = repoUsers.createUser("Charlie", "charlie@isel.pt", PasswordValidationInfo("hash5"))
            val init = Instant.now().minusSeconds(60)
            val t1 = Token(TokenValidationInfo("t1"), user.id, init, Instant.now().minusSeconds(10))
            val t2 = Token(TokenValidationInfo("t2"), user.id, init, Instant.now().minusSeconds(5))
            val t3 = Token(TokenValidationInfo("t3"), user.id, init, Instant.now())
            repoUsers.createToken(t1, maxTokens = 2)
            repoUsers.createToken(t2, maxTokens = 2)
            repoUsers.createToken(t3, maxTokens = 2)
            // t1 should be removed
            assertNull(repoUsers.getTokenByTokenValidationInfo(TokenValidationInfo("t1")))
            assertNotNull(repoUsers.getTokenByTokenValidationInfo(TokenValidationInfo("t2")))
            assertNotNull(repoUsers.getTokenByTokenValidationInfo(TokenValidationInfo("t3")))
        }
    }

    @Test
    fun `updateTokenLastUsed replaces token`() {
        trxManager.run {
            val user = repoUsers.createUser("Eve", "eve@isel.pt", PasswordValidationInfo("hash6"))
            val info = TokenValidationInfo("tokenEve")
            val init = Instant.now().truncatedTo(SECONDS).minusSeconds(200)
            val tokenOld = Token(info, user.id, init, init.plusSeconds(100))
            repoUsers.createToken(tokenOld, maxTokens = 2)
            val tokenNew = Token(info, user.id, init, Instant.now().truncatedTo(SECONDS))
            repoUsers.updateTokenLastUsed(tokenNew, tokenNew.lastUsedAt)
            val result: Pair<User, Token>? = repoUsers.getTokenByTokenValidationInfo(info)
            assertNotNull(result)
            assertEquals(tokenNew, result.second)
        }
    }

    @Test
    fun `removeTokenByValidationInfo removes token`() {
        trxManager.run {
            val user = repoUsers.createUser("Gregorius", "gregorius@isel.pt", PasswordValidationInfo("hash7"))
            val info = TokenValidationInfo("tokenGregorius")
            val token = Token(info, user.id, Instant.now(), Instant.now())
            repoUsers.createToken(token, maxTokens = 2)
            val removed = repoUsers.removeTokenByValidationInfo(info)
            assertEquals(1, removed)
            assertNull(repoUsers.getTokenByTokenValidationInfo(info))
        }
    }
}
