package pt.isel.pdm.pokerDice.services.utils

import pt.isel.pdm.pokerDice.domain.TokenValidationInfo

class SimpleTokenEncoder : TokenEncoder {
    private val encoder = SimpleSha256PasswordEnconder()
    override fun createValidationInformation(token: String): TokenValidationInfo =
        TokenValidationInfo(encoder.encode(token))
}