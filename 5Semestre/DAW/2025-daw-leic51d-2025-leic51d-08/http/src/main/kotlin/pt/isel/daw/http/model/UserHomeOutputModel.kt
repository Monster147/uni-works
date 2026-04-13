package pt.isel.daw.http.model

data class UserHomeOutputModel(
    val id: Int,
    val name: String,
    val email: String,
    val balance: Int,
)
