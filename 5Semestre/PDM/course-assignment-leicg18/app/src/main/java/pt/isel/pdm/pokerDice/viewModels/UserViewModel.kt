package pt.isel.pdm.pokerDice.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.pdm.pokerDice.domain.Invitation
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.domain.UserStats
import pt.isel.pdm.pokerDice.services.InvitationServicesInterface
import pt.isel.pdm.pokerDice.services.InviteError
import pt.isel.pdm.pokerDice.services.Success
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfoRepo
import pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure.AuthInfoPreferencesRepo
import pt.isel.pdm.pokerDice.viewModels.utils.viewModelFactory

interface ProfileState {
    object Idle : ProfileState
    object Loading : ProfileState
    data class Success(val user: User) : ProfileState
    object Unauthenticated : ProfileState
}

class UserViewModel(
    private val userService: UserServiceInterface,
    private val authInfoRepo: AuthInfoRepo,
    private val invitationServices: InvitationServicesInterface,
) : ViewModel() {

    companion object {
        fun getFactory(
            userServices: UserServiceInterface,
            authInfoRepo: AuthInfoRepo,
            invitationServices: InvitationServicesInterface
        ) =
            viewModelFactory {
                UserViewModel(
                    userServices,
                    authInfoRepo,
                    invitationServices
                )
            }
    }

    var user by mutableStateOf<User?>(null)
        private set

    var userStats by mutableStateOf<UserStats?>(null)
        private set

    var state: ProfileState by mutableStateOf(ProfileState.Idle)
        private set

    var code by mutableStateOf("")
        private set

    var validCode by mutableStateOf(true)
        private set

    init {
        observeAuthInfo()
    }

    private fun observeAuthInfo() {
        viewModelScope.launch {
            authInfoRepo.authInfo.collect { authInfo ->
                if (authInfo != null) {
                    fetchUser()
                } else {
                    user = null
                    state = ProfileState.Unauthenticated
                }
            }
        }
    }

    fun fetchUser() {
        if (state is ProfileState.Loading || state is ProfileState.Success) {
            return
        }

        state = ProfileState.Loading

        viewModelScope.launch {
            val info = authInfoRepo.getAuthInfo()
            if (info != null) {
                val result = userService.getUserByToken(info.authToken)
                state = if (result != null) {
                    user = result
                    val userId = user?.id
                    if(userId != null) userStats = userService.getUserStats(userId)
                    ProfileState.Success(result)
                } else {
                    user = null
                    ProfileState.Unauthenticated
                }
            } else {
                user = null
                state = ProfileState.Unauthenticated
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val info = authInfoRepo.getAuthInfo()
            if (info != null){
                userService.revokeToken(info.authToken)
                authInfoRepo.clearAuthInfo()
                state = ProfileState.Unauthenticated
            }
        }
    }

    fun getCode(){
        viewModelScope.launch{
            val info = authInfoRepo.getAuthInfo()
            if (info != null) {
                val userResult = userService.getUserByToken(info.authToken)
                if (userResult != null) {
                    val invitationResult = invitationServices.createInvitation(userResult.id)
                    if (invitationResult is Success) code = invitationResult.value
                }
            }
        }
    }

    fun validateCode(){
        viewModelScope.launch{
            val result = invitationServices.isValid(code)
            if (result is Success) {
                validCode = result.value
            } else {
                validCode = false
            }
        }
    }
}