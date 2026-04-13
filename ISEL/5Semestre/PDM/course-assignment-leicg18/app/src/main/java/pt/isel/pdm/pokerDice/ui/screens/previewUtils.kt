package pt.isel.pdm.pokerDice.ui.screens

import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.InvitationServices
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.MatchServices
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.MatchViewModel
import pt.isel.pdm.pokerDice.viewModels.UserViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyDetailsViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.RegisterViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

val previewTrxManager = TransactionManagerInMem()
val previewPasswordEncoder = SimpleSha256PasswordEnconder()
val previewTokenEncoder = SimpleTokenEncoder()
val previewUserConfig = UsersDomainConfig()
val previewClock = Clock.systemUTC()

val previewUserServices = UserServices(
    previewPasswordEncoder,
    previewTokenEncoder,
    previewUserConfig,
    previewTrxManager,
    previewClock
)

val previewAuthInfoRepo = FakeAuthInfoRepo()
val previewLobbyServices = LobbyServices(previewTrxManager)

val previewMatchServices = MatchServices(previewTrxManager)

val previewInvitationServices = InvitationServices(previewTrxManager)

val loginViewModelPreview = LoginViewModel(previewUserServices, previewAuthInfoRepo)

val registerViewModelPreview = RegisterViewModel(previewUserServices)

val userViewModelPreview = UserViewModel(
    previewUserServices,
    previewAuthInfoRepo,
    previewInvitationServices
)

val lobbyViewModelPreview = LobbyViewModel(
    previewLobbyServices,
    previewUserServices,
    previewAuthInfoRepo
)

val matchViewModelPreview = MatchViewModel(
    previewMatchServices,
    previewUserServices,
    previewAuthInfoRepo,
    previewLobbyServices
)

val lobbyCreationViewModelPreview = LobbyCreationViewModel(
    previewLobbyServices,
    previewUserServices,
    previewAuthInfoRepo
)

val lobbyDetailsViewModelPreview = LobbyDetailsViewModel(
    previewLobbyServices,
    previewUserServices,
    previewMatchServices,
    previewAuthInfoRepo
)