package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
data class LobbySSEData(
    val id: Int,
    val action: String,
    val data: JsonElement
)

sealed interface LobbyEvent {
    data class UserJoined(val payload: LobbyChangedUserPayload) : LobbyEvent
    data class UserLeft(val payload: LobbyChangedUserPayload) : LobbyEvent
    data class LobbyDeleted(val reason: String) : LobbyEvent
    data class MatchStarted(val payload: MatchStartedPayload) : LobbyEvent
    data class Error(val message: String) : LobbyEvent
}
