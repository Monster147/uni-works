package pt.isel.pdm.pokerDice.domain

import java.time.Instant

data class Invitation(
    val code: String,
    val createdBy: Int?,
    val createdAt: Instant = Instant.now(),
    val used: Boolean = false,
)
