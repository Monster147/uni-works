package pt.isel.daw.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.daw.AuthenticatedUser
import pt.isel.daw.Lobby
import pt.isel.daw.http.model.LobbyInput
import pt.isel.daw.http.model.LobbyOutput
import pt.isel.daw.http.model.Problem
import pt.isel.daw.service.ActionKind
import pt.isel.daw.service.DataPublisher
import pt.isel.daw.service.Failure
import pt.isel.daw.service.LobbyError
import pt.isel.daw.service.LobbyServices
import pt.isel.daw.service.Success

@RestController
class LobbyController(
    private val lobbyServices: LobbyServices,
    private val publisher: DataPublisher,
) {
    private fun getLobbySummary(lobby: Lobby): LobbyOutput {
        return LobbyOutput(
            id = lobby.id,
            name = lobby.name,
            description = lobby.description,
            nOfPlayers = "${lobby.players.size}/${lobby.maxPlayers}",
            host = lobby.host,
            players = lobby.players,
            state = lobby.state,
            ante = lobby.ante,
            nOfRounds = lobby.rounds,
            matchId = lobby.matchId,
            autoStartAt = lobby.autoStartAt,
        )
    }

    // user needs to be authenticated
    @PostMapping("/api/lobbies")
    fun createLobby(
        user: AuthenticatedUser,
        @RequestBody lobbyInput: LobbyInput,
    ): ResponseEntity<*> {
        val lobbyResult =
            lobbyServices.createLobby(
                lobbyInput.name,
                lobbyInput.description,
                lobbyInput.max_players,
                user.user,
                lobbyInput.rounds,
                lobbyInput.ante,
            )
        return when (lobbyResult) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(
                        "Location",
                        "/api/lobbies/${lobbyResult.value.id}",
                    )
                    .body(lobbyResult.value.id)
            is Failure -> handleLobbyError(lobbyResult.value)
        }
    }

    @PostMapping("/api/lobbies/{id}/join")
    fun joinLobby(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val lobbyResult = lobbyServices.joinLobby(id, user.user)
        return when (lobbyResult) {
            is Success -> {
                val lobby = lobbyResult.value
                publisher.sendMessageToAll(
                    "Lobby-${lobby.id}",
                    mapOf(
                        "lobby" to getLobbySummary(lobby),
                        "changedUser" to user.user,
                    ),
                    ActionKind.UserJoined,
                )
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(
                        "Location",
                        "/api/lobbies/${lobby.id}",
                    ).build<Unit>()
            }

            is Failure -> handleLobbyError(lobbyResult.value)
        }
    }

    @PostMapping("/api/lobbies/{id}/leave")
    fun leaveLobby(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val lobbyResult = lobbyServices.leaveLobby(id, user.user)
        return when (lobbyResult) {
            is Success -> {
                val lobby = lobbyResult.value
                when (lobby.host.id == user.user.id) {
                    true ->
                        publisher.sendMessageToAll(
                            "Lobby-${lobby.id}",
                            "Host has left. Lobby is deleted.",
                            ActionKind.LobbyDeleted,
                        )
                    false ->
                        publisher.sendMessageToAll(
                            "Lobby-${lobby.id}",
                            mapOf(
                                "lobby" to getLobbySummary(lobby),
                                "changedUser" to user.user,
                            ),
                            ActionKind.UserLeft,
                        )
                }
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(
                        "Location",
                        "/api/lobbies",
                    ).build<Unit>()
            }

            is Failure -> handleLobbyError(lobbyResult.value)
        }
    }

    @GetMapping("/api/lobbies/available")
    fun getAllAvailableLobbies(): ResponseEntity<*> {
        val lobbies = lobbyServices.getAllAvailableLobbies().map { getLobbySummary(it) }
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(lobbies)
    }

    @GetMapping("/api/lobbies/all")
    fun getAllLobbies(): ResponseEntity<*> {
        val lobbies = lobbyServices.getAllLobbies().map { getLobbySummary(it) }
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(lobbies)
    }

    @GetMapping("/api/lobbies/{id}")
    fun getLobbyById(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val lobbyResult = lobbyServices.getLobbyById(id)
        return when (lobbyResult) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(getLobbySummary(lobbyResult.value))

            is Failure -> handleLobbyError(lobbyResult.value)
        }
    }

    @PostMapping("/api/lobbies/{id}/delete")
    fun deleteLobby(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val lobbyResult = lobbyServices.deleteLobby(id, user.user)
        return when (lobbyResult) {
            is Success -> {
                publisher.sendMessageToAll(
                    "Lobby-$id",
                    "Lobby has been deleted",
                    ActionKind.LobbyDeleted,
                )
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(
                        "Location",
                        "/api/lobbies",
                    ).build<Unit>()
            }

            is Failure -> handleLobbyError(lobbyResult.value)
        }
    }

    @GetMapping("/api/lobbies/{id}/events")
    fun subscribeToLobbyEvents(
        @PathVariable id: Int,
    ): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        publisher.addEmitter(
            "Lobby-$id",
            SSEUpdateDataEmitterAdapter(emitter),
        )
        return emitter
    }

    private fun handleLobbyError(error: LobbyError): ResponseEntity<*> =
        when (error) {
            LobbyError.LobbyNameAlreadyInUse -> Problem.LobbyNameAlreadyInUse.response(HttpStatus.BAD_REQUEST)
            LobbyError.NotFound -> Problem.LobbyNotFound.response(HttpStatus.NOT_FOUND)
            LobbyError.NotHost -> Problem.NotHost.response(HttpStatus.FORBIDDEN)
        }
}
