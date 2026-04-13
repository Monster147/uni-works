package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.Serializable

@Serializable
data class LobbyChangedUserPayload(
    val lobby: LobbyOutputDTO,
    val changedUser: UserDTO
)

@Serializable
data class MatchStartedPayload(
    val message: String,
    val matchId: Int
)