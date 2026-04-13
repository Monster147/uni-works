package pt.isel.daw

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import pt.isel.daw.repo.jdbc.DatabaseConnection
import pt.isel.daw.repo.jdbc.TransactionManagerJdbc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryInvitationJdbcTest {
    companion object {
        val trxManager = TransactionManagerJdbc()
        val con = DatabaseConnection.getConnection()
    }

    @AfterAll
    fun teardownDatabase() {
        con.close()
    }

    @BeforeEach
    fun cleanTable() {
        con.createStatement().use { stmt ->
            stmt.execute("DELETE FROM dbo.invites")
        }
    }

    @Test
    fun `create returns a non-empty invitation with correct creator`() {
        trxManager.run {
            val invite = repoInvitations.create(1)
            assertNotNull(invite)
            assertTrue(invite.code.isNotBlank())
            assertEquals(1, invite.createdBy)
            assertFalse(invite.used)
            assertNotNull(invite.createdAt)
        }
    }

    @Test
    fun `findByCode returns invitation if exists`() {
        trxManager.run {
            val invite = repoInvitations.create(1)
            val found = repoInvitations.findByCode(invite.code)
            assertNotNull(found)
            assertEquals(invite.code, found.code)
            assertEquals(invite.createdBy, found.createdBy)
        }
    }

    @Test
    fun `findByCode returns null if code does not exist`() {
        trxManager.run {
            val found = repoInvitations.findByCode("nonexistent")
            assertNull(found)
        }
    }

    @Test
    fun `consume returns true if invitation was unused`() {
        trxManager.run {
            val invite = repoInvitations.create(1)
            val result = repoInvitations.consume(invite.code)
            assertTrue(result)

            val updated = repoInvitations.findByCode(invite.code)
            assertNotNull(updated)
            assertTrue(updated.used)
        }
    }

    @Test
    fun `consume returns false if invitation was already used`() {
        trxManager.run {
            val invite = repoInvitations.create(1)
            repoInvitations.consume(invite.code)
            val secondConsume = repoInvitations.consume(invite.code)
            assertFalse(secondConsume)
        }
    }

    @Test
    fun `consume returns false if code does not exist`() {
        trxManager.run {
            val result = repoInvitations.consume("nonexistent")
            assertFalse(result)
        }
    }

    @Test
    fun `create throws if createdBy is null`() {
        assertThrows(IllegalArgumentException::class.java) {
            trxManager.run {
                repoInvitations.create(null)
            }
        }
    }
}
