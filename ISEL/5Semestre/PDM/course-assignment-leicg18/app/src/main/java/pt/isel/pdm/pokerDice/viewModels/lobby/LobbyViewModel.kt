package pt.isel.pdm.pokerDice.viewModels.lobby

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.services.LobbyServiceInterface
import pt.isel.pdm.pokerDice.services.Success
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfoRepo
import pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure.AuthInfoPreferencesRepo
import pt.isel.pdm.pokerDice.viewModels.utils.viewModelFactory

class LobbyViewModel(
    private val lobbyService: LobbyServiceInterface,
    private val userService: UserServiceInterface,
    private val authInfo: AuthInfoRepo,
) : ViewModel() {


    companion object {
        fun getFactory(
            lobbyServices: LobbyServiceInterface,
            userServices: UserServiceInterface,
            authInfo: AuthInfoPreferencesRepo
        ) =
            viewModelFactory {
                LobbyViewModel(
                    lobbyServices,
                    userServices,
                    authInfo
                )
            }
    }

    var lobbiesList by mutableStateOf<List<Lobby>>(emptyList())
        private set

    private val _allLobbies = MutableStateFlow<List<Lobby>>(emptyList())
    val allLobbies: StateFlow<List<Lobby>> = _allLobbies.asStateFlow()

    private val _availableLobbies = MutableStateFlow<List<Lobby>>(emptyList())
    val availableLobbies: StateFlow<List<Lobby>> = _availableLobbies.asStateFlow()

    fun fetchLobbies() {
        viewModelScope.launch {
            lobbyService.getAllLobbies().collect { list ->
                lobbiesList = list
                _allLobbies.value = list
            }
            delay(4000)
        }
    }

    fun fetchAvailableLobbies() {
        viewModelScope.launch {
            while (true) {
                lobbyService.getAllAvailableLobbies().collect { list ->
                    _availableLobbies.value = list
                }
                delay(10000)
            }
        }
    }

    fun joinLobby(lobby: Lobby) {
        viewModelScope.launch {
            val randomUser = User(
                id = (1000..9999).random(),
                name = "User${(1000..9999).random()}",
                email = "user${(1000..9999).random()}@example.com",
                passwordValidation = PasswordValidationInfo("hash")
            )
            val playerToJoin = authInfo.getAuthInfo()?.let {
                userService.getUserByToken(it.authToken)
            } ?: randomUser
            val updatedLobby = lobbyService.joinLobby(lobby.id, playerToJoin)
            if (updatedLobby is Success) fetchLobbies()
        }
    }
}