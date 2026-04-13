package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.Serializable
import pt.isel.pdm.pokerDice.domain.TokenExternalInfo
import java.time.Instant

@Serializable
data class TokenExternalInfoDTO(
    val token: String,
){
    fun toTokenExternalInfo() = TokenExternalInfo(
        tokenValue = this.token,
    )
}