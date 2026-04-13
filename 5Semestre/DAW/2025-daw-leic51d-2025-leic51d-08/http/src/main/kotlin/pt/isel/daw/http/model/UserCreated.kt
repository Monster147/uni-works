package pt.isel.daw.http.model

import pt.isel.daw.PasswordValidationInfo

data class UserCreated(
    val id: Int,
    val name: String,
    val email: String,
    val password: PasswordValidationInfo,
    val balance: Int,
)
