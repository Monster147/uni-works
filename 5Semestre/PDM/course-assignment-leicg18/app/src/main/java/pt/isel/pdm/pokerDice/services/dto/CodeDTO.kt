package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.Serializable
import pt.isel.pdm.pokerDice.domain.Invitation

@Serializable
class CodeDTO (
    val code: String
) {
    fun toInvite() : Invitation {
        return Invitation(
            code = this.code,
            createdBy = null
        )
    }
}