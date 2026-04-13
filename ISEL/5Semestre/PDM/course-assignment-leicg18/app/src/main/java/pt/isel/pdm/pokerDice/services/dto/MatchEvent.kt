package pt.isel.pdm.pokerDice.services.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MatchPayload(
    val match: MatchDTO
)

@Serializable
data class MatchSSEData(
    val id: Int,
    val data: JsonElement,
    val action: String
)

sealed interface MatchEvent {
    data class NewRound(val payload: MatchPayload) : MatchEvent
    data class PlayedTurn(val payload: MatchPayload) : MatchEvent
    data class PassedTurn(val payload: MatchPayload) : MatchEvent
    data class RoundEnded(val payload: MatchPayload) : MatchEvent
    data class MatchEnded(val payload: MatchPayload) : MatchEvent
    data class Error(val message: String) : MatchEvent
}

