package pt.isel.pdm.pokerDice.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.Round
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.services.dto.LobbyEvent
import pt.isel.pdm.pokerDice.services.dto.LobbySSEData
import pt.isel.pdm.pokerDice.services.dto.MatchDTO
import pt.isel.pdm.pokerDice.services.dto.MatchEvent
import pt.isel.pdm.pokerDice.services.dto.MatchPayload
import pt.isel.pdm.pokerDice.services.dto.MatchSSEData
import pt.isel.pdm.pokerDice.services.dto.RoundDTO
import pt.isel.pdm.pokerDice.services.utils.APIConfig

class MatchAPIServices(
    private val client: HttpClient,
    private val getToken: suspend () -> String?,
    private val json: Json = Json { ignoreUnknownKeys = true }
): MatchServiceInterface {
    private val baseURL = APIConfig.BASE_URL

    override suspend fun startMatch(
        lobbyId: Int,
        host: User
    ): Either<MatchError, Match> {
        try{
            val token = getToken() ?: return failure(MatchError.UnknownError)
            val response = client.post("$baseURL/api/lobbies/$lobbyId/start"){
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            when(response.status.value){
                201 -> {
                    val location = response.headers[HttpHeaders.Location]
                    val matchId = location?.substringAfterLast("/")?.toIntOrNull()
                        ?: return failure(MatchError.UnknownError)
                    return getMatchById(matchId)?.let { match ->
                        success(match)
                    } ?: failure(MatchError.UnknownError)
                }
                403 -> {
                    return failure(MatchError.NotHostOfLobby)
                }
                404 -> {
                    return failure(MatchError.MatchLobbyNotFound)
                }
                else -> {
                    return failure(MatchError.UnknownError)
                }
            }
        } catch(e: Exception){
            return failure(MatchError.UnknownError)
        }
        //TODO("Not yet implemented")
    }

    override suspend fun startNextRound(matchId: Int): Either<MatchError, Match> {
        try {
            val token = getToken() ?: return failure(MatchError.UnknownError)
            val response = client.post("$baseURL/api/matches/$matchId/rounds/start"){
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            return when(response.status.value){
                200 -> {
                    getMatchById(matchId)?.let { match ->
                        success(match)
                    } ?: failure(MatchError.UnknownError)
                }

                404 -> {
                    failure(MatchError.MatchLobbyNotFound)
                }

                else -> {
                    failure(MatchError.UnknownError)
                }
            }
        } catch (e: Exception) {
            return failure(MatchError.UnknownError)
        }
        //TODO("Not yet implemented")
    }

    override suspend fun playTurn(
        matchId: Int,
        player: User,
        keptDice: List<Boolean>?
    ): Either<MatchError, Match> {
        try {
            val token = getToken() ?: return failure(MatchError.UnknownError)
            val response = client.post("$baseURL/api/matches/$matchId/turns/play"){
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                if(keptDice != null){
                    val keptDiceJson = keptDice.joinToString(
                        prefix = "[",
                        postfix = "]",
                        separator = ","
                    ) { it.toString() }
                    setBody("""{"kept_dice": $keptDiceJson}""")
                } else {
                    setBody("""{}""")
                }
            }
            return when(response.status.value){
                200 -> {
                    getMatchById(matchId)?.let { match ->
                        success(match)
                    } ?: failure(MatchError.UnknownError)
                }

                404 -> {
                    failure(MatchError.MatchLobbyNotFound)
                }

                else -> {
                    failure(MatchError.UnknownError)
                }
            }
        } catch (e: Exception) {
            println(e)
            return failure(MatchError.UnknownError)
        }
        //TODO("Not yet implemented")
    }

    override suspend fun passTurn(
        matchId: Int,
        player: User
    ): Either<MatchError, Match> {
        try {
            val token = getToken() ?: return failure(MatchError.UnknownError)
            val response = client.post("$baseURL/api/matches/$matchId/turns/pass"){
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            return when(response.status.value){
                200 -> {
                    getMatchById(matchId)?.let { match ->
                        success(match)
                    } ?: failure(MatchError.UnknownError)
                }

                404 -> {
                    failure(MatchError.MatchLobbyNotFound)
                }

                else -> {
                    failure(MatchError.UnknownError)
                }
            }
        } catch (e: Exception) {
            return failure(MatchError.UnknownError)
        }
        //TODO("Not yet implemented")
    }

    override suspend fun finishRound(
        matchId: Int,
        host: User
    ): Either<MatchError, Match> {
        try {
            val token = getToken() ?: return failure(MatchError.UnknownError)
            val response =  client.post("$baseURL/api/matches/$matchId/finish"){
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            return when(response.status.value){
                200 -> {
                    getMatchById(matchId)?.let { match ->
                        success(match)
                    } ?: failure(MatchError.UnknownError)
                }

                404 -> {
                    failure(MatchError.MatchLobbyNotFound)
                }

                else -> {
                    failure(MatchError.UnknownError)
                }
            }
        } catch (e: Exception) {
            return failure(MatchError.UnknownError)
        }
        //TODO("Not yet implemented")
    }

    override suspend fun getMatchById(matchId: Int): Match? {
        try {
            val response = client.get("$baseURL/api/matches/$matchId"){
                contentType(ContentType.Application.Json)
            }
            return when(response.status.value) {
                200 -> {
                    val matchDTO = response.body<MatchDTO>()
                    println("Match: $matchDTO")
                    matchDTO.toMatch()
                }

                404 -> {
                    return null
                }

                else -> {
                    return null
                }
            }
        } catch (e: Exception) {
            println(e)
            return null
        }
        //TODO("Not yet implemented")
    }

    override suspend fun getRoundById(
        matchId: Int,
        roundNumber: Int
    ): Round? {
        try {
            val response = client.get("$baseURL/api/matches/$matchId/rounds/$roundNumber"){
                contentType(ContentType.Application.Json)
            }
            return when(response.status.value) {
                200 -> {
                    val roundDTO = response.body<RoundDTO>()
                    println("Round: $roundDTO")
                    roundDTO.toRound(matchId)
                }

                404 -> {
                    return null
                }

                else -> {
                    return null
                }
            }
        } catch (e: Exception) {
            return null
        }
        //TODO("Not yet implemented")
    }

    override fun subscribeToMatchEvents(matchId: Int): Flow<MatchEvent> = flow {
        println("Subscribing to match SSE for matchId: $matchId")
        client.sse("$baseURL/api/matches/$matchId/events") {
            println("Established SSE connection for matchId: $matchId")
            incoming.collect { sseEvent ->
                println("Received SSE event: $sseEvent")
                val sseData = sseEvent.data
                if(sseData != null) {
                    println("SSE Data preview: ${sseData.take(150)}")
                    try{
                        val envelope = json.decodeFromString<MatchSSEData>(sseData)
                        when(envelope.action){
                            "NewRound" -> {
                                val payload = json.decodeFromJsonElement<MatchPayload>(envelope.data)
                                emit(MatchEvent.NewRound(payload))
                            }
                            "PlayedTurn" -> {
                                val payload = json.decodeFromJsonElement<MatchPayload>(envelope.data)
                                emit(MatchEvent.PlayedTurn(payload))
                            }
                            "PassedTurn" -> {
                                val payload = json.decodeFromJsonElement<MatchPayload>(envelope.data)
                                emit(MatchEvent.PassedTurn(payload))
                            }
                            "RoundEnded" -> {
                                val payload = json.decodeFromJsonElement<MatchPayload>(envelope.data)
                                emit(MatchEvent.RoundEnded(payload))
                            }
                            "MatchEnded" -> {
                                val payload = json.decodeFromJsonElement<MatchPayload>(envelope.data)
                                emit(MatchEvent.MatchEnded(payload))
                            }
                            else -> {
                                emit(MatchEvent.Error("Unknown match SSE action: ${envelope.action}"))
                            }
                        }
                    } catch (e: Exception){
                        emit(MatchEvent.Error("Failed to parse match SSE: ${e.message}"))
                    }
                }
            }
        }
    }
}