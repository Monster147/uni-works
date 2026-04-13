package pt.isel.pdm.pokerDice.viewModels.lobby

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.LobbyServiceInterface
import pt.isel.pdm.pokerDice.services.MatchServiceInterface
import pt.isel.pdm.pokerDice.services.Success
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.dto.LobbyEvent
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfoRepo
import pt.isel.pdm.pokerDice.viewModels.utils.viewModelFactory

sealed interface MatchState {
    object Idle : MatchState
    object Loading : MatchState
    data class Success(val matchId: Int) : MatchState
    data class Error(val message: String) : MatchState
}

class LobbyDetailsViewModel(
    private val lobbyService: LobbyServiceInterface,
    private val userService: UserServiceInterface,
    private val matchService: MatchServiceInterface,
    private val authInfo: AuthInfoRepo,
) : ViewModel() {
    companion object {
        fun getFactory(
            lobbyServices: LobbyServiceInterface,
            userServices: UserServiceInterface,
            matchServices: MatchServiceInterface,
            authInfo: AuthInfoRepo
        ) =
            viewModelFactory {
                LobbyDetailsViewModel(
                    lobbyServices,
                    userServices,
                    matchServices,
                    authInfo
                )
            }
    }

    private val _currentLobby = MutableStateFlow<Lobby?>(null)
    val currentLobby: StateFlow<Lobby?> = _currentLobby.asStateFlow()

    private val _lobbyEvents = MutableSharedFlow<LobbyEvent>()

    private val _navigateBack = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateBack = _navigateBack.asSharedFlow()

    fun startListeningToLobbyEvents(lobbyId: Int) {
        viewModelScope.launch {
            lobbyService.subscribeToLobbyEvents(lobbyId).collect { event ->
                when (event) {
                    is LobbyEvent.UserJoined -> {
                        _currentLobby.value =
                            event.payload.lobby.toLobby()
                    }
                    is LobbyEvent.UserLeft -> {
                        _currentLobby.value =
                            event.payload.lobby.toLobby()
                    }

                    is LobbyEvent.LobbyDeleted -> {
                        println("Lobby deleted: ${event.reason}")
                        _navigateBack.tryEmit(Unit)
                    }

                    is LobbyEvent.MatchStarted -> {
                        matchState =
                            MatchState.Success(event.payload.matchId)
                    }

                    is LobbyEvent.Error -> {
                        println("Lobby SSE error: ${event.message}")
                    }
                }

                _lobbyEvents.emit(event)
            }
        }
    }

    fun fetchLobbyById(id: Int) {
        viewModelScope.launch {
            val lobby = lobbyService.getLobbyById(id)
            if (lobby is Success) {
                _currentLobby.value = lobby.value
            }
        }
    }

    var isHost by mutableStateOf(false)
        private set

    fun checkHost() {
        viewModelScope.launch {
            authInfo.getAuthInfo()?.let {
                isHost = currentLobby.value?.host?.id == userService.getUserByToken(it.authToken)?.id
            }
        }
    }

    fun leaveLobby(lobby: Lobby) {
        viewModelScope.launch {
            if (lobby.players.isNotEmpty()) {
                val playerToRemove = authInfo.getAuthInfo()?.let {
                    userService.getUserByToken(it.authToken)
                } ?: lobby.players.last()
                lobbyService.leaveLobby(lobby.id, playerToRemove)
            }
        }
    }

    var matchState: MatchState by mutableStateOf(MatchState.Idle)
        private set

    fun startMatch(lobby: Lobby) {
        if (matchState is MatchState.Loading || matchState is MatchState.Success) return

        matchState = MatchState.Loading

        viewModelScope.launch {
            val result = matchService.startMatch(lobby.id, lobby.host)
            matchState = when (result) {
                is Either.Left -> MatchState.Error("Failed to start match")
                is Either.Right -> {
                    MatchState.Success(result.value.id)
                }
            }
        }
    }
}