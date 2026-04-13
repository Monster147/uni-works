package pt.isel.pdm.pokerDice.services.utils

import pt.isel.pdm.pokerDice.domain.TokenValidationInfo

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}