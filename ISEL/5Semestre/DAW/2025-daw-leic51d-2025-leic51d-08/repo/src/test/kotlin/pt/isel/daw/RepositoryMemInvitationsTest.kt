package pt.isel.daw

import org.junit.jupiter.api.BeforeEach
import pt.isel.daw.repo.RepositoryInvitation
import pt.isel.daw.repo.mem.RepositoryInvitationInMem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryMemInvitationsTest {
    private lateinit var repo: RepositoryInvitation

    @BeforeEach
    fun setup() {
        repo = RepositoryInvitationInMem()
    }

    @Test
    fun `create returns invitation with correct createdBy and unused`() {
        val inv = repo.create(1)
        assertNotNull(inv)
        assertEquals(1, inv.createdBy)
        assertFalse(inv.used)
        assertTrue(inv.code.isNotBlank())
    }

    @Test
    fun `findByCode returns the correct invitation`() {
        val inv = repo.create(1)
        val found = repo.findByCode(inv.code)
        assertNotNull(found)
        assertEquals(inv.code, found.code)
        assertEquals(inv.createdBy, found.createdBy)
    }

    @Test
    fun `findByCode returns null for non-existent code`() {
        val found = repo.findByCode("nonexistent")
        assertNull(found)
    }

    @Test
    fun `consume marks invitation as used and returns true`() {
        val inv = repo.create(1)
        val result = repo.consume(inv.code)
        assertTrue(result)

        val updated = repo.findByCode(inv.code)
        assertNotNull(updated)
        assertTrue(updated.used)
    }

    @Test
    fun `consume returns false if invitation already used`() {
        val inv = repo.create(1)
        repo.consume(inv.code)
        val secondConsume = repo.consume(inv.code)
        assertFalse(secondConsume)
    }

    @Test
    fun `consume returns false if code does not exist`() {
        val result = repo.consume("nonexistent")
        assertFalse(result)
    }

    @Test
    fun `multiple invitations do not interfere with each other`() {
        val inv1 = repo.create(1)
        val inv2 = repo.create(2)

        assertTrue(repo.consume(inv1.code))
        assertFalse(repo.findByCode(inv1.code)?.used == false)

        val found2 = repo.findByCode(inv2.code)
        assertNotNull(found2)
        assertFalse(found2.used)
    }
}
