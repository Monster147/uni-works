package pt.isel.pdm.pokerDice.viewModels.login_register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.pdm.pokerDice.domain.UserCredentials
import pt.isel.pdm.pokerDice.domain.UserRegisterCredentials
import pt.isel.pdm.pokerDice.services.Failure
import pt.isel.pdm.pokerDice.services.Success
import pt.isel.pdm.pokerDice.services.UserError
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.viewModels.utils.viewModelFactory

interface RegisterState {
    object Idle : RegisterState
    object Loading : RegisterState
    data class Success(val credentials: UserCredentials) : RegisterState
    data class Error(val message: String) : RegisterState
}

class RegisterViewModel(
    private val userService: UserServiceInterface
) : ViewModel() {
    companion object {
        fun getFactory(service: UserServiceInterface) =
            viewModelFactory { RegisterViewModel(service) }
    }

    var registerState: RegisterState by mutableStateOf(RegisterState.Idle)
        private set


    fun addUser(userRegisterCredentials: UserRegisterCredentials) {
        if (registerState is RegisterState.Loading || registerState is RegisterState.Success) {
            return
        }

        registerState = RegisterState.Loading

        viewModelScope.launch {

            val result = userService.createUser(
                userRegisterCredentials.name,
                userRegisterCredentials.email,
                userRegisterCredentials.password,
                userRegisterCredentials.code
            )

            registerState =
                when (result) {
                    is Success -> {
                        RegisterState.Success(
                            UserCredentials(
                                userRegisterCredentials.email,
                                userRegisterCredentials.password
                            )
                        )
                    }

                    is Failure -> {
                        when (result.value) {
                            is UserError.AlreadyUsedEmailAddress ->
                                RegisterState.Error("Email address is already in use")

                            is UserError.InsecurePassword ->
                                RegisterState.Error("The password is not secure enough")
                        }
                    }
                }
        }
    }
}