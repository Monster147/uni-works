package pt.isel.pdm.pokerDice.services.utils

import java.security.MessageDigest
import java.util.Base64

class SimpleSha256PasswordEnconder : PasswordEnconder {
    override fun encode(raw: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray(Charsets.UTF_8))
            .let { Base64.getEncoder().encodeToString(it) }

    override fun matches(raw: String, encoded: String): Boolean =
        encode(raw) == encoded
}