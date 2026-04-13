package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.Serializable

@Serializable
data class InvitationValidationDTO(
    val valid: Boolean
)