package pt.isel.daw.http.model

data class UserCreateTokenInputModel(
    val email: String,
    val password: String,
)
