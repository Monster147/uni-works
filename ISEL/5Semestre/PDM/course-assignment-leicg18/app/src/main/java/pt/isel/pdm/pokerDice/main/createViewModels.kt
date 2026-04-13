package pt.isel.pdm.pokerDice.main

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.isel.pdm.pokerDice.services.InvitationAPIServices
import pt.isel.pdm.pokerDice.services.LobbyAPIServices
import pt.isel.pdm.pokerDice.services.MatchAPIServices
import pt.isel.pdm.pokerDice.services.UsersAPIServices
import pt.isel.pdm.pokerDice.viewModels.LoadingSplashViewModel
import pt.isel.pdm.pokerDice.viewModels.MatchViewModel
import pt.isel.pdm.pokerDice.viewModels.UserViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyDetailsViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.RegisterViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure.AuthInfoPreferencesRepo

@Composable
fun createLobbiesViewModel(
    allServices: Map<String, Any>
) : LobbyViewModel {
    val lobbyServices = allServices["lobbyServices"] as LobbyAPIServices
    val userServices = allServices["userServices"] as UsersAPIServices
    val authInfoRepo = allServices["authInfoRepo"] as AuthInfoPreferencesRepo

    return viewModel(
        factory = LobbyViewModel.getFactory(
            lobbyServices,
            userServices,
            authInfoRepo
        )
    )
}

@Composable
fun createLobbyDetailsViewModel(
    allServices: Map<String, Any>
) : LobbyDetailsViewModel {
    val lobbyServices = allServices["lobbyServices"] as LobbyAPIServices
    val userServices = allServices["userServices"] as UsersAPIServices
    val matchServices = allServices["matchServices"] as MatchAPIServices
    val authInfoRepo = allServices["authInfoRepo"] as AuthInfoPreferencesRepo

    return viewModel(
        factory = LobbyDetailsViewModel.getFactory(
            lobbyServices,
            userServices,
            matchServices,
            authInfoRepo
        )
    )
}

@Composable
fun createLobbyCreationViewModel(
    allServices: Map<String, Any>
) : LobbyCreationViewModel {
    val lobbyServices = allServices["lobbyServices"] as LobbyAPIServices
    val userServices = allServices["userServices"] as UsersAPIServices
    val authInfoRepo = allServices["authInfoRepo"] as AuthInfoPreferencesRepo

    return viewModel(
        factory = LobbyCreationViewModel.getFactory(
            lobbyServices,
            userServices,
            authInfoRepo
        )
    )
}

@Composable
fun createUserViewModel(
    allServices: Map<String, Any>
) : UserViewModel {
    val userServices = allServices["userServices"] as UsersAPIServices
    val authInfoRepo = allServices["authInfoRepo"] as AuthInfoPreferencesRepo
    val invitationServices = allServices["invitationServices"] as InvitationAPIServices

    return viewModel(
        factory = UserViewModel.getFactory(
            userServices,
            authInfoRepo,
            invitationServices
        )
    )
}

@Composable
fun createMatchViewModel(
    allServices: Map<String, Any>
) : MatchViewModel {
    val lobbyServices = allServices["lobbyServices"] as LobbyAPIServices
    val userServices = allServices["userServices"] as UsersAPIServices
    val matchServices = allServices["matchServices"] as MatchAPIServices
    val authInfoRepo = allServices["authInfoRepo"] as AuthInfoPreferencesRepo

    return viewModel(
        factory = MatchViewModel.getFactory(
            matchServices,
            userServices,
            authInfoRepo,
            lobbyServices
        )
    )
}

@Composable
fun createLoginViewModel(
    allServices: Map<String, Any>
) : LoginViewModel {
    val userServices = allServices["userServices"] as UsersAPIServices
    val authInfoRepo = allServices["authInfoRepo"] as AuthInfoPreferencesRepo

    return viewModel(
        factory = LoginViewModel.getFactory(
            userServices,
            authInfoRepo
        )
    )
}

@Composable
fun createRegisterViewModel(
    allServices: Map<String, Any>
) : RegisterViewModel {
    val userServices = allServices["userServices"] as UsersAPIServices

    return viewModel(
        factory = RegisterViewModel.getFactory(
            userServices
        )
    )
}

@Composable
fun createLoadingSplashViewModel(
    allServices: Map<String, Any>
) : LoadingSplashViewModel {
    val userServices = allServices["userServices"] as UsersAPIServices
    val authInfoRepo = allServices["authInfoRepo"] as AuthInfoPreferencesRepo

    return viewModel(
        factory = LoadingSplashViewModel.getFactory(
            userServices,
            authInfoRepo
        )
    )
}

