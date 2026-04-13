package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateLobbyInput(
    val name: String,
    val description: String,
    @SerialName("max_players")
    val maxPlayers: Int,
    val rounds: Int,
    val ante: Int
)
