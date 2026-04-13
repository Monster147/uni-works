package pt.isel.daw.http.model

data class UserInput(
    val name: String,
    val email: String,
    val password: String,
    val invite_code: String,
)
