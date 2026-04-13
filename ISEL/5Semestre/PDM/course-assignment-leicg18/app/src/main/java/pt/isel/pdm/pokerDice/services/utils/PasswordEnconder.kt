package pt.isel.pdm.pokerDice.services.utils

interface PasswordEnconder {
    fun encode(raw: String): String
    fun matches(raw: String, encoded: String): Boolean
}