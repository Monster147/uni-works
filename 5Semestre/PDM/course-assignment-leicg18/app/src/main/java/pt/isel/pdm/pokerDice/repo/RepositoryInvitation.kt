package pt.isel.pdm.pokerDice.repo

import pt.isel.pdm.pokerDice.domain.Invitation

interface RepositoryInvitation {
    fun create(createdBy: Int?): Invitation

    fun findByCode(code: String): Invitation?

    fun consume(code: String): Boolean
}
