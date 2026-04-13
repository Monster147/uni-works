package pt.isel.pdm.pokerDice.main

import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.UserCredentials
import pt.isel.pdm.pokerDice.services.LobbyAPIServices
import pt.isel.pdm.pokerDice.services.MatchAPIServices
import pt.isel.pdm.pokerDice.services.UsersAPIServices
import pt.isel.pdm.pokerDice.ui.LoadingSplash
import pt.isel.pdm.pokerDice.ui.screens.AboutScreen
import pt.isel.pdm.pokerDice.ui.screens.ProfileScreen
import pt.isel.pdm.pokerDice.ui.screens.TitleScreen
import pt.isel.pdm.pokerDice.ui.screens.game.GameScreen
import pt.isel.pdm.pokerDice.ui.screens.lobby.LobbyCreationScreen
import pt.isel.pdm.pokerDice.ui.screens.lobby.LobbyDetailsScreen
import pt.isel.pdm.pokerDice.ui.screens.lobby.LobbyScreen
import pt.isel.pdm.pokerDice.ui.screens.login_register.LoginScreen
import pt.isel.pdm.pokerDice.ui.screens.login_register.RegisterScreen
import pt.isel.pdm.pokerDice.viewModels.MatchViewModel
import pt.isel.pdm.pokerDice.viewModels.ProfileState
import pt.isel.pdm.pokerDice.viewModels.UserViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.RegisterViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfoRepo
import pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure.AuthInfoPreferencesRepo
import kotlin.getValue

enum class PokerDiceScreen() {
    Login,
    Register,
    Title,
    Lobbies,
    Profile,
    About,
    LobbyCreation,
    Lobby,
    Loading
}

const val LOBBY_DETAILS_TAG = "LobbyDetails/{lobbyId}"

const val MATCH_TAG = "Match/{matchId}"

@Composable
fun PokerDiceApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onRulesClick: () -> Unit = { },
    onGmailClick: () -> Unit = { },
    allServices: Map<String, Any>
) {
    NavHost(
        navController = navController,
        startDestination = PokerDiceScreen.Loading.name
    ) {
        composable(PokerDiceScreen.Loading.name) {
            val loadingSplashViewModel = createLoadingSplashViewModel(allServices)
            LoadingSplash(
                modifier = modifier,
                loadingSplashViewModel = loadingSplashViewModel,
                onNavigateToLogin = {
                    navController.navigate(PokerDiceScreen.Login.name) {
                        remove()
                    }
                },
                onNavigateToTitle = {
                    navController.navigate(PokerDiceScreen.Title.name) {
                        remove()
                    }
                }
            )
        }

        composable(PokerDiceScreen.Login.name) {
            val loginViewModel = createLoginViewModel(allServices)
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToTitle = {
                    navController.navigate(PokerDiceScreen.Title.name) {
                        remove()
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(PokerDiceScreen.Register.name) {
                        remove()
                    }
                },
                modifier = modifier
            )
        }

        composable(PokerDiceScreen.Register.name) {
            val loginViewModel = createLoginViewModel(allServices)
            val registerViewModel = createRegisterViewModel(allServices)
            RegisterScreen(
                registerViewModel = registerViewModel,
                loginViewModel = loginViewModel,
                onNavigateBack = {
                    navController.navigate(PokerDiceScreen.Login.name) {
                        remove()
                    }
                },
                onNavigateToTitle = {
                    navController.navigate(PokerDiceScreen.Title.name)  {
                        remove()
                    }
                },
                modifier = modifier
            )
        }

        composable(PokerDiceScreen.Title.name) {
            TitleScreen(
                onNavigateToAbout = { navController.navigate(PokerDiceScreen.About.name) },
                onNavigateToLobbies = {
                    navController.navigate(PokerDiceScreen.Lobbies.name) {
                        remove()
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(PokerDiceScreen.Profile.name) {
                        remove()
                    }
                },
                modifier = modifier
            )
        }

        composable(route = PokerDiceScreen.Lobbies.name) {
            val lobbyViewModel: LobbyViewModel = createLobbiesViewModel(allServices)
            LobbyScreen(
                onNavigateToTitle = {
                    navController.navigate(PokerDiceScreen.Title.name) {
                        remove()
                    }
                },
                onNavigateToLobbyCreation = {
                    navController.navigate(PokerDiceScreen.LobbyCreation.name){
                        remove()
                    }
                },
                onNavigateToLobbyDetails = {
                    navController.navigate("LobbyDetails/$it") {
                        remove()
                    }
                },
                modifier = modifier,
                viewModel = lobbyViewModel
            )
        }

        composable(route = PokerDiceScreen.Profile.name) {
            val userViewModel = createUserViewModel(allServices)
            ProfileScreen(
                onNavigateToTitle = {
                    navController.navigate(PokerDiceScreen.Title.name) {
                        remove()
                    }
                },
                modifier = modifier,
                userViewModel = userViewModel,
                onLogout = {
                    navController.navigate(PokerDiceScreen.Login.name) {
                        remove()
                    }
                }
            )
        }

        composable(route = PokerDiceScreen.About.name) {
            AboutScreen(
                onNavigateToTitle = {
                    navController.navigate(PokerDiceScreen.Title.name) {
                        remove()
                    }
                },
                modifier = modifier,
                onRulesClick = onRulesClick,
                onGmailClick = onGmailClick
            )
        }

        composable(route = PokerDiceScreen.LobbyCreation.name) {
            val lobbyCreationViewModel = createLobbyCreationViewModel(allServices)
            LobbyCreationScreen(
                viewModel = lobbyCreationViewModel,
                onNavigateToLobbies = {
                    navController.navigate(PokerDiceScreen.Lobbies.name) {
                        remove()
                    }
                },
                onNavigateToLobbyDetails = {
                    navController.navigate("LobbyDetails/$it") {
                        remove()
                    }
                },
                modifier = modifier,
            )
        }

        composable(LOBBY_DETAILS_TAG) { backStackEntry ->
            val lobbyDetailViewModel = createLobbyDetailsViewModel(allServices)
            val lobbyId = backStackEntry.arguments?.getString("lobbyId")?.toInt() ?: -1
            LobbyDetailsScreen(
                onNavigateToLobbies = {
                    lobbyDetailViewModel.currentLobby.let {
                        val lobby = it.value
                        if(lobby != null) lobbyDetailViewModel.leaveLobby(lobby)
                    }
                    navController.navigate(PokerDiceScreen.Lobbies.name) {
                        remove()
                    }
                },
                modifier = modifier,
                lobbyId = lobbyId,
                lobbyDetailsViewModel = lobbyDetailViewModel,
                onNavigateToMatch = { matchId ->
                    navController.navigate("Match/$matchId") {
                        remove()
                    }
                }
            )
        }

        composable(MATCH_TAG) { backStackEntry ->
            val matchViewModelNew = createMatchViewModel(allServices)
            val matchId = backStackEntry.arguments?.getString("matchId")?.toInt() ?: -1
            GameScreen(
                viewModel = matchViewModelNew,
                modifier = modifier,
                matchId = matchId,
                onNavigateToLobbies = {
                    navController.navigate(PokerDiceScreen.Lobbies.name) {
                        remove()
                    }
                }
            )
        }
    }
}

private fun NavOptionsBuilder.remove() = popUpTo(0) { inclusive = true }