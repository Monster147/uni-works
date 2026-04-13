package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.Serializable
import pt.isel.pdm.pokerDice.domain.User

@Serializable
data class UserDTO(
    val id: Int,
    val name: String,
    val email: String,
    val balance: Int,
) {
    fun toUser() = User(
        id = this.id,
        name = this.name,
        email = this.email,
        balance = this.balance,
    )
}