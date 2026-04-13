package pt.isel.pdm.pokerDice.domain


data class User(
    val id: Int,
    val name: String,
    val email: String,
    val passwordValidation: PasswordValidationInfo = PasswordValidationInfo(""),
    var balance: Int = 500,
) {
    init {
        require(Regex("^[a-zA-Z0-9._-]{3,20}\$").matches(name)) { "Invalid name format" }
        require(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$").matches(email)) { "Invalid email format" }
    }
}
