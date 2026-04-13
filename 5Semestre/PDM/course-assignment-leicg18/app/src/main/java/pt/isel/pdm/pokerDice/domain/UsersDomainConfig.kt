package pt.isel.pdm.pokerDice.domain

import java.time.Duration

data class UsersDomainConfig(
    val tokenSizeInBytes: Int = 32,
    val tokenTtl: Duration = Duration.ofDays(7),
    val tokenRollingTtl: Duration = Duration.ofDays(1),
    val maxTokensPerUser: Int = 5,
)