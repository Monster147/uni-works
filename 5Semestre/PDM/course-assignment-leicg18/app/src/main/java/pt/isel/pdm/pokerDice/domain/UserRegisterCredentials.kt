package pt.isel.pdm.pokerDice.domain

data class UserRegisterCredentials(val name: String, val email: String, val password: String, val code: String) {
    init {
        require(
            value = isValidRegisterCredentialsData(
                name,
                email,
                password,
                code
            )
        ) { "Invalid user register credentials: $this" }
    }
}

fun String.isValidUsername(): Boolean = this.matches(Regex("^[a-zA-Z0-9._-]{3,20}$"))

fun isValidRegisterCredentialsData(name: String, email: String, password: String, code: String): Boolean =
    name.isValidUsername() && email.isValidEmail() && password.isValidPassword() && code.isNotBlank()
