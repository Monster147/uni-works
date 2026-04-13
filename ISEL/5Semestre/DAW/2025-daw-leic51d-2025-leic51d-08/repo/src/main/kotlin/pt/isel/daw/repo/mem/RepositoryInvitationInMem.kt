package pt.isel.daw.repo.mem

import pt.isel.daw.Invitation
import pt.isel.daw.repo.RepositoryInvitation
import java.util.UUID

class RepositoryInvitationInMem : RepositoryInvitation {
    private val invitations = hashMapOf<String, Invitation>()

    override fun create(createdBy: Int?): Invitation {
        val code = UUID.randomUUID().toString()
        val inv = Invitation(code, createdBy)
        invitations[code] = inv
        return inv
    }

    override fun findByCode(code: String): Invitation? = invitations[code]

    override fun consume(code: String): Boolean {
        while (true) {
            val inv = invitations[code] ?: return false
            if (inv.used) return false
            val usedInv = inv.copy(used = true)
            val result = invitations.replace(code, inv, usedInv)
            if (result) return true
        }
    }
}
