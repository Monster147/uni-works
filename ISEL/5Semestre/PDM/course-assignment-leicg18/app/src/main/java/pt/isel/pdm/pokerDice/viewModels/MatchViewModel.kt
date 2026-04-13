package pt.isel.pdm.pokerDice.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Round
import pt.isel.pdm.pokerDice.domain.TurnState
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.LobbyServiceInterface
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.MatchError
import pt.isel.pdm.pokerDice.services.MatchServiceInterface
import pt.isel.pdm.pokerDice.services.MatchServices
import pt.isel.pdm.pokerDice.services.Success
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.dto.LobbyEvent
import pt.isel.pdm.pokerDice.services.dto.MatchEvent
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfoRepo
import pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure.AuthInfoPreferencesRepo
import pt.isel.pdm.pokerDice.viewModels.utils.viewModelFactory
import kotlin.math.round

enum class SseConnectionState {
    CONNECTING,
    CONNECTED,
    ERROR
}

sealed interface HostState {
    object Loading : HostState
    object Host : HostState
    object NotHost : HostState
}

class MatchViewModel(
    private val matchServices: MatchServiceInterface,
    private val userServices: UserServiceInterface,
    private val authInfo: AuthInfoRepo,
    private val lobbyServices: LobbyServiceInterface
) : ViewModel() {
    companion object {
        fun getFactory(
            matchServices: MatchServiceInterface,
            userServices: UserServiceInterface,
            authInfo: AuthInfoPreferencesRepo,
            lobbyServices: LobbyServiceInterface
        ) =
            viewModelFactory {
                MatchViewModel(matchServices, userServices, authInfo, lobbyServices)
            }
    }

    private val _currentMatch = MutableStateFlow<Match?>(null)
    val currentMatch: StateFlow<Match?> = _currentMatch.asStateFlow()

    private val _currentRound = MutableStateFlow<Round?>(null)
    val currentRound: StateFlow<Round?> = _currentRound.asStateFlow()

    private val _hostState = MutableStateFlow<HostState>(HostState.Loading)
    val hostState: StateFlow<HostState> = _hostState

    var currUserLoggedIn by mutableStateOf<User?>(null)
        private set

    fun fetchMatch(matchId: Int) {
        viewModelScope.launch {
            _hostState.value = HostState.Loading

            val match = matchServices.getMatchById(matchId)
            _currentMatch.value = match

            val user = authInfo.getAuthInfo()?.let {
                currUserLoggedIn = userServices.getUserByToken(it.authToken)
                currUserLoggedIn
            }

            if (match == null || user == null) {
                _hostState.value = HostState.NotHost
                return@launch
            }


            val lobby = match.let { lobbyServices.getLobbyById(it.lobbyId) }
            if (lobby is Success) {
                _hostState.value =
                    if (lobby.value.host.id == user.id)
                        HostState.Host
                    else
                        HostState.NotHost
            } else {
                _hostState.value = HostState.NotHost
            }
        }
    }

    fun startNextRound(matchId: Int) {
        viewModelScope.launch {
            matchServices.startNextRound(matchId)
        }
    }

    fun playTurn(
        matchId: Int,
        player: User,
        diceToRoll: List<Boolean>? = null
    ) {
        viewModelScope.launch {
            delay(500)
            matchServices.playTurn(matchId, player, diceToRoll)
        }
    }

    fun passTurn(
        matchId: Int,
        player: User,
    ) {
        viewModelScope.launch {
            matchServices.passTurn(matchId, player)
        }
    }

    fun endCurrentRound(matchId: Int) {
        viewModelScope.launch {
            val round = currentRound.value ?: return@launch

            val turnsCompleted =
                round.turns.isNotEmpty() &&
                        round.turns.all { it.state == TurnState.COMPLETED }

            val canEnd =
                hostState.value is HostState.Host &&
                        turnsCompleted

            if(canEnd) {
                val user = currUserLoggedIn
                if(user != null) matchServices.finishRound(matchId, user)
            }
        }
    }

    private val _matchEvents = MutableSharedFlow<MatchEvent>()

    private val _sseState = MutableStateFlow(SseConnectionState.CONNECTING)
    val sseState: StateFlow<SseConnectionState> = _sseState

    fun startListeningToMatchEvents(matchId: Int) {
        viewModelScope.launch {
            _sseState.value = SseConnectionState.CONNECTING
            matchServices.subscribeToMatchEvents(matchId)
                .onStart {
                    _sseState.value = SseConnectionState.CONNECTED
                }
                .catch {
                    _sseState.value = SseConnectionState.ERROR
                }
                .collect { event ->
                    when (event) {
                        is MatchEvent.NewRound -> {
                            val match = event.payload.match.toMatch()
                            _currentMatch.value = match
                            val round = match.rounds.find { it.roundNumber == match.currentRound }
                            _currentRound.value = round
                        }

                        is MatchEvent.PlayedTurn -> {
                            val match = event.payload.match.toMatch()
                            _currentMatch.value = match
                            val round = match.rounds.find { it.roundNumber == match.currentRound }
                            _currentRound.value = round
                            if (hostState.value is HostState.Host) {
                                endCurrentRound(match.id)
                            }
                        }

                        is MatchEvent.PassedTurn -> {
                            val match = event.payload.match.toMatch()
                            _currentMatch.value = match
                            val round = match.rounds.find { it.roundNumber == match.currentRound }
                            _currentRound.value = round
                            if (hostState.value is HostState.Host) {
                                endCurrentRound(match.id)
                            }
                        }

                        is MatchEvent.RoundEnded -> {
                            val match = event.payload.match.toMatch()
                            _currentMatch.value = match
                            val round = match.rounds.find { it.roundNumber == match.currentRound }
                            _currentRound.value = round
                            if(hostState.value is HostState.Host) {
                                delay(1500)
                                startNextRound(match.id)
                            }
                        }

                        is MatchEvent.MatchEnded -> {
                            delay(1000)
                            val match = event.payload.match.toMatch()
                            _currentMatch.value = match
                        }

                        is MatchEvent.Error -> {
                            println("Match SSE error: ${event.message}")
                        }
                    }
                    _matchEvents.emit(event)
            }
        }
    }
}