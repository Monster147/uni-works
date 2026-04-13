package pt.isel.pdm.pokerDice.domain

import android.util.Patterns

data class UserCredentials(val email: String, val password: String) {
    init {
        require(
            value = isValidCredentialsData(
                email,
                password
            )
        ) { "Invalid user credentials: $this" }
    }
}

const val SPECIAL_CHARACTERS = "!@#\$%^&*()-_=+[]{}|\\:;\"'<>,.?/"

fun String.isValidEmail(): Boolean =
    isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPassword(): Boolean = length >= 8 &&
        any { it.isDigit() } &&
        any { it.isLowerCase() } &&
        any { it.isUpperCase() } &&
        any { SPECIAL_CHARACTERS.contains(it) }

fun isValidCredentialsData(email: String, password: String): Boolean =
    email.isValidEmail() && password.isValidPassword()