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
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.services.dto.CreateLobbyInput
import pt.isel.pdm.pokerDice.services.dto.LobbyChangedUserPayload
import pt.isel.pdm.pokerDice.services.dto.LobbyEvent
import pt.isel.pdm.pokerDice.services.dto.LobbyOutputDTO
import pt.isel.pdm.pokerDice.services.dto.LobbySSEData
import pt.isel.pdm.pokerDice.services.dto.MatchStartedPayload
import pt.isel.pdm.pokerDice.services.utils.APIConfig

class LobbyAPIServices(
    private val client: HttpClient,
    private val getToken: suspend () -> String?,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : LobbyServiceInterface {

    private val baseUrl = APIConfig.BASE_URL

    override suspend fun createLobby(
        name: String,
        description: String,
        maxPlayers: Int,
        host: User, // unused
        rounds: Int,
        ante: Int
    ): Either<LobbyError, Lobby> {
        try {
            val token = getToken() ?: return failure(LobbyError.NotFound)
            val response = client.post("$baseUrl/api/lobbies") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    CreateLobbyInput(
                        name = name,
                        description = description,
                        maxPlayers = maxPlayers,
                        rounds = rounds,
                        ante = ante
                    )
                )
            }

            when(response.status){
                HttpStatusCode.Created -> {
                    val location = response.headers[HttpHeaders.Location]
                    val id = location?.substringAfterLast("/")?.toIntOrNull()
                        ?: return failure(LobbyError.NotFound)
                    return getLobbyById(id)
                }
                HttpStatusCode.Conflict -> {
                    return failure(LobbyError.LobbyNameAlreadyInUse)
                }
                else -> {
                    return failure(LobbyError.NotFound)
                }
            }
        } catch (e: Exception) {
            println(e)
            return failure(LobbyError.NotFound)
        }
    }

    override suspend fun joinLobby(
        lobbyID: Int,
        player: User // unused
    ): Either<LobbyError, Lobby> {
        try{
            val token = getToken() ?: return failure(LobbyError.NotFound)
            val response = client.post("$baseUrl/api/lobbies/$lobbyID/join"){
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            when(response.status){
                HttpStatusCode.OK -> {
                    val location = response.headers[HttpHeaders.Location]
                    val id = location?.substringAfterLast("/")?.toIntOrNull()
                        ?: return failure(LobbyError.NotFound)
                    return getLobbyById(id)
                }
                HttpStatusCode.NotFound -> {
                    return failure(LobbyError.NotFound)
                }
                else -> {
                    return failure(LobbyError.NotFound)
                }
            }

        } catch (e: Exception) {
            return failure(LobbyError.NotFound)
        }
        //TODO("Not yet implemented")
    }

    override suspend fun leaveLobby(
        lobbyID: Int,
        player: User
    ): Either<LobbyError, Lobby> {
        try{
            val token = getToken() ?: return failure(LobbyError.NotFound)
            val response = client.post("$baseUrl/api/lobbies/$lobbyID/leave"){
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            println("Leave Lobby Response: $response")
            when(response.status){
                HttpStatusCode.OK -> {
                    return success(
                        Lobby(
                            lobbyID,
                            "lobbyRemoved",
                            "Lobby $lobbyID was removed",
                            2,
                            player,
                            0,
                            mutableListOf(),
                            0
                        )
                    )
                }
                HttpStatusCode.NotFound -> {
                    println("Lobby not found")
                    return failure(LobbyError.NotFound)
                }
                else -> {
                    return failure(LobbyError.NotFound)
                }
            }

        } catch (e: Exception) {
            return failure(LobbyError.NotFound)
        }
        //TODO("Not yet implemented")
    }

    override fun getAllLobbies(): Flow<List<Lobby>> {
        try {
            return flow {
                val response = client.get("$baseUrl/api/lobbies/all") {
                    contentType(ContentType.Application.Json)
                }.body<List<LobbyOutputDTO>>()
                emit(response.map { it.toLobby() } )
            }
        } catch (e: Exception) {
            return flow {
                emit(emptyList())
            }
        }
    }

    override fun getAllAvailableLobbies(): Flow<List<Lobby>> {
        try {
            return flow {
                val response = client.get("$baseUrl/api/lobbies/available") {
                    contentType(ContentType.Application.Json)
                }.body<List<LobbyOutputDTO>>()
                emit(response.map { it.toLobby() } )
            }
        } catch (e: Exception) {
            return flow {
                emit(emptyList())
            }
        }
    }

    override suspend fun getLobbyById(lobbyID: Int): Either<LobbyError, Lobby> {
        try{
            val response = client.get("$baseUrl/api/lobbies/$lobbyID") {
                contentType(ContentType.Application.Json)
            }
            return when(response.status){
                HttpStatusCode.OK -> {
                    val lobby = response.body<LobbyOutputDTO>().toLobby()
                    success(lobby)
                }
                HttpStatusCode.NotFound -> {
                    failure(LobbyError.NotFound)
                }
                else -> {
                    failure(LobbyError.NotFound)
                }
            }
        } catch(e: Exception){
            return failure(LobbyError.NotFound)
        }
    }

    override suspend fun deleteLobby(
        lobbyID: Int,
        requester: User
    ): Either<LobbyError, Boolean> {
        try{
            val token = getToken() ?: return failure(LobbyError.NotFound)
            val response = client.post("$baseUrl/api/lobbies/$lobbyID/delete"){
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            return when(response.status){
                HttpStatusCode.OK -> {
                    success(true)
                }
                HttpStatusCode.NotFound -> {
                    failure(LobbyError.NotFound)
                }
                HttpStatusCode.Forbidden -> {
                    failure(LobbyError.NotHost)
                }
                else -> {
                    failure(LobbyError.NotFound)
                }
            }

        } catch (e: Exception) {
            return failure(LobbyError.NotFound)
        }
        //TODO("Not yet implemented")
    }

    override fun subscribeToLobbyEvents(lobbyID: Int): Flow<LobbyEvent> = flow  {
        client.sse("$baseUrl/api/lobbies/$lobbyID/events") {
            incoming.collect { sseEvent ->
                println("Received SSE event: $sseEvent")

                val sseData = sseEvent.data
                if (sseData != null) {
                    println("SSE Data preview: ${sseData.take(150)}")

                    try {
                        val envelope = json.decodeFromString<LobbySSEData>(sseData)
                        when(envelope.action){
                            "UserJoined" -> {
                                val payload = json.decodeFromJsonElement<LobbyChangedUserPayload>(envelope.data)
                                emit(LobbyEvent.UserJoined(payload))
                            }
                            "UserLeft" -> {
                                val payload = json.decodeFromJsonElement<LobbyChangedUserPayload>(envelope.data)
                                emit(LobbyEvent.UserLeft(payload))
                            }

                            "LobbyDeleted" -> {
                                val reason =
                                    envelope.data.jsonPrimitive.content
                                emit(LobbyEvent.LobbyDeleted(reason))
                            }

                            "MatchStarted" -> {
                                val payload =
                                    json.decodeFromJsonElement<MatchStartedPayload>(
                                        envelope.data
                                    )
                                emit(LobbyEvent.MatchStarted(payload))
                            }
                            else -> {
                                emit(LobbyEvent.Error("Unknown lobby action: ${envelope.action}"))
                            }
                        }
                    } catch (e: Exception) {
                        emit(LobbyEvent.Error("Failed to parse lobby SSE: ${e.message}"))
                    }
                }
            }
        }
    }
}