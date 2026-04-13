package pt.isel.pdm.pokerDice.viewModels.lobby

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.LobbyCreation
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.LobbyError
import pt.isel.pdm.pokerDice.services.LobbyServiceInterface
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfoRepo
import pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure.AuthInfoPreferencesRepo
import pt.isel.pdm.pokerDice.viewModels.utils.viewModelFactory

interface LobbyCreationState {
    object Idle : LobbyCreationState
    data class Loading(val tentativeDetails: LobbyCreation) : LobbyCreationState
    data class Success(val lobby: Lobby) : LobbyCreationState
    data class Error(val message: String) : LobbyCreationState
}

class LobbyCreationViewModel(
    private val lobbyService: LobbyServiceInterface,
    private val userService: UserServiceInterface,
    private val authInfo: AuthInfoRepo
) : ViewModel() {

    companion object {
        fun getFactory(
            lobbyServices: LobbyServiceInterface,
            userServices: UserServiceInterface,
            authInfo: AuthInfoPreferencesRepo
        ) =
            viewModelFactory { LobbyCreationViewModel(lobbyServices, userServices, authInfo) }
    }

    var lobbyCreationState: LobbyCreationState by mutableStateOf(LobbyCreationState.Idle)
        private set

    fun createLobby(lobbyCreation: LobbyCreation) {
        if (lobbyCreationState is LobbyCreationState.Loading ||
            lobbyCreationState is LobbyCreationState.Success
        ) {
            return
        }

        lobbyCreationState = LobbyCreationState.Loading(lobbyCreation)

        viewModelScope.launch {
            val user = authInfo.getAuthInfo()?.let {
                userService.getUserByToken(it.authToken)
            } ?: User(
                id = 1,
                name = "TestUser",
                email = "qqlcoisa@qqlcoisa.com",
                passwordValidation = PasswordValidationInfo("hash")
            )

            val result = lobbyService.createLobby(
                lobbyCreation.name,
                lobbyCreation.description,
                lobbyCreation.maxPlayers,
                user,
                lobbyCreation.nOfRounds,
                lobbyCreation.ante
            )

            lobbyCreationState = when (result) {
                is Either.Left ->
                    when (result.value) {
                        LobbyError.LobbyNameAlreadyInUse -> LobbyCreationState.Error("Failed to create lobby: Lobby name already in use!")// LobbyCreationState.Error("User is not authorized to create a lobby.")
                        else -> LobbyCreationState.Error("Failed to create lobby: Unexpected Error")
                    }

                is Either.Right ->
                    LobbyCreationState.Success(result.value)
            }
        }
    }

    // Usado apenas nos testes, caso queira criar mais que um lobby num único teste
    fun resetLobbyCreationState() {
        lobbyCreationState = LobbyCreationState.Idle
    }
}