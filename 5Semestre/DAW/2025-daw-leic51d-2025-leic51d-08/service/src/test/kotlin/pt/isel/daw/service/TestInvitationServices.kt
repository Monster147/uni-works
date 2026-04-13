package pt.isel.daw.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.isel.daw.repo.TransactionManager
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class TestInvitationServices {
    @Autowired
    private lateinit var invitationServices: InvitationServices

    @Autowired
    private lateinit var trxManager: TransactionManager

    @Test
    fun `createInvitation returns a non-empty code`() {
        val code =
            invitationServices.createInvitation(createdBy = 1).let {
                check(it is Either.Right)
                it.value
            }
        assertTrue(code.isNotBlank())
    }

    @Test
    fun `isValid returns true for a fresh invite`() {
        val validInvite =
            invitationServices.createInvitation(1).let {
                check(it is Either.Right)
                it.value
            }

        val result =
            invitationServices.isValid(validInvite).let {
                check(it is Either.Right)
                it.value
            }
        assertTrue(result)
    }

    @Test
    fun `isValid returns NotFound for non-existent invite`() {
        val result =
            invitationServices.isValid("nonexistentcode").let {
                check(it is Either.Left)
                it.value
            }
        assertTrue(result is InviteError.NotFound)
    }

    @Test
    fun `isValid returns AlreadyUsed for consumed invite`() {
        val validInvite =
            invitationServices.createInvitation(1).let {
                check(it is Either.Right)
                it.value
            }

        // consumir o convite
        trxManager.run {
            repoInvitations.consume(validInvite)
        }

        val result =
            invitationServices.isValid(validInvite).let {
                check(it is Either.Left)
                it.value
            }
        assertTrue(result is InviteError.AlreadyUsed)
    }

    @Test
    fun `cannot consume invite twice`() {
        val code =
            invitationServices.createInvitation(1).let {
                check(it is Either.Right)
                it.value
            }

        val firstConsume =
            trxManager.run {
                repoInvitations.consume(code)
            }
        val secondConsume =
            trxManager.run {
                repoInvitations.consume(code)
            }

        assertTrue(firstConsume)
        assertFalse(secondConsume)
    }

    @Test
    fun `isValid fails if invite code is empty string`() {
        val result =
            invitationServices.isValid("").let {
                check(it is Either.Left)
                it.value
            }
        assertTrue(result is InviteError.NotFound)
    }
}
