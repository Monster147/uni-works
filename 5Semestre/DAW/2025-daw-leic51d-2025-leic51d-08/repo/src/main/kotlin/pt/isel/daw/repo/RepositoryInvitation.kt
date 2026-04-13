package pt.isel.daw.repo

import pt.isel.daw.Invitation

interface RepositoryInvitation {
    fun create(createdBy: Int?): Invitation

    fun findByCode(code: String): Invitation?

    fun consume(code: String): Boolean
}
