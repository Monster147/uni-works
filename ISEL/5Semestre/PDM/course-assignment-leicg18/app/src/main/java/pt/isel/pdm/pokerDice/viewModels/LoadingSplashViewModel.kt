package pt.isel.pdm.pokerDice.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.pdm.pokerDice.domain.UserCredentials
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfo
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfoRepo
import pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure.AuthInfoPreferencesRepo
import pt.isel.pdm.pokerDice.viewModels.utils.viewModelFactory

interface LoadingLoginState {
    object Idle : LoadingLoginState
    data class Loading(val tentativeCredentials: UserCredentials) : LoadingLoginState
    data class Success(val token: String) : LoadingLoginState
    data class Error(val message: String) : LoadingLoginState
    object NoLogin : LoadingLoginState
}

class LoadingSplashViewModel (
    private val userService: UserServiceInterface,
    private val authInfoRepo: AuthInfoRepo
): ViewModel() {
    companion object {
        fun getFactory(userServices: UserServiceInterface, authInfoRepo: AuthInfoPreferencesRepo) =
            viewModelFactory { LoadingSplashViewModel( userServices, authInfoRepo) }
    }

    var loadingLoginState: LoadingLoginState by mutableStateOf(LoadingLoginState.Idle)
        private set

    fun loginStoredCredentials() {
        if(loadingLoginState is LoadingLoginState.Loading || loadingLoginState is LoadingLoginState.Success)
            return

        viewModelScope.launch {
            val storedAuthInfo = authInfoRepo.getAuthInfo()
            if(storedAuthInfo != null) {

                loadingLoginState = LoadingLoginState.Loading(
                    UserCredentials(
                        email = storedAuthInfo.userEmail,
                        password = storedAuthInfo.password
                    )
                )

                val result = userService.createToken(
                    storedAuthInfo.userEmail,
                    storedAuthInfo.password
                )

                loadingLoginState = when (result) {
                    is Either.Left -> {
                        LoadingLoginState.Error("Invalid email or password")
                    }

                    is Either.Right -> {
                        authInfoRepo.saveAuthInfo(
                            AuthInfo(
                                userEmail = storedAuthInfo.userEmail,
                                authToken = result.value.tokenValue,
                                password = storedAuthInfo.password
                            )
                        )
                        LoadingLoginState.Success(result.value.tokenValue)
                    }
                }
            } else {
                loadingLoginState = LoadingLoginState.NoLogin
            }
        }
    }


}