package pt.isel.pdm.pokerDice.viewModels.login_register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.pdm.pokerDice.domain.UserCredentials
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.TokenCreationError
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfo
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfoRepo
import pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure.AuthInfoPreferencesRepo
import pt.isel.pdm.pokerDice.viewModels.utils.viewModelFactory

interface LoginState {
    object Idle : LoginState
    data class Loading(val tentativeCredentials: UserCredentials) : LoginState
    data class Success(val token: String) : LoginState
    data class Error(val message: String) : LoginState
}

class LoginViewModel(
    private val userService: UserServiceInterface,
    private val authInfoRepo: AuthInfoRepo
) : ViewModel() {
    companion object {
        fun getFactory(service: UserServiceInterface, authInfoRepo: AuthInfoRepo) =
            viewModelFactory { LoginViewModel(service, authInfoRepo) }
    }

    var loginState: LoginState by mutableStateOf(LoginState.Idle)
        private set


    fun login(userCredentials: UserCredentials) {

        if (loginState is LoginState.Loading || loginState is LoginState.Success) {
            return
        }

        loginState = LoginState.Loading(userCredentials)

        viewModelScope.launch {
            val result = userService.createToken(
                userCredentials.email,
                userCredentials.password
            )

            loginState = when (result) {
                is Either.Left -> when (result.value) {
                    TokenCreationError.UserOrPasswordAreInvalid ->
                        LoginState.Error("Invalid email or password")
                }

                is Either.Right -> {
                    authInfoRepo.saveAuthInfo(
                        AuthInfo(
                            userEmail = userCredentials.email,
                            authToken = result.value.tokenValue,
                            password = userCredentials.password
                        )
                    )
                    LoginState.Success(result.value.tokenValue)
                }
            }
        }
    }
}