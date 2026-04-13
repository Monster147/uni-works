package pt.isel.pdm.pokerDice.ui.screens.lobby

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.main.PokerDiceScreen
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.LobbyServiceInterface
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.MatchServices
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.ui.BACK_BUTTON_TAG
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyDetailsViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfo
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

class LobbyDetailsScreenTestsHost {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeLobbyService: LobbyServiceInterface

    private lateinit var fakeUserService: UserServiceInterface
    lateinit var navController: TestNavHostController
    lateinit var lobbyDetailsViewModel: LobbyDetailsViewModel

    val trxManager = TransactionManagerInMem()
    val passwordEncoder = SimpleSha256PasswordEnconder()
    val tokenEncoder = SimpleTokenEncoder()
    val userConfig = UsersDomainConfig()
    val clock = Clock.systemUTC()
    val authentication = FakeAuthInfoRepo()

    @Before
    fun setupAppNavHost() = runBlocking {
        fakeLobbyService = LobbyServices(TransactionManagerInMem())

        fakeUserService =  UserServices(
            passwordEncoder,
            tokenEncoder,
            userConfig,
            trxManager,
            clock
        )

        val token = fakeUserService.createToken("alice@gmail.com", "hash")

        if(token is Either.Right) {
            authentication.saveAuthInfo(
                AuthInfo(
                    userEmail = "alice@gmail.com",
                    authToken = token.value.tokenValue,
                    password = "irrelevant-for-ui-tests"
                )
            )
        }

        lobbyDetailsViewModel = LobbyDetailsViewModel(
            fakeLobbyService,
            fakeUserService,
            MatchServices(
                trxManager
            ), authentication
        )
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())


            val graph = navController.createGraph(startDestination = PokerDiceScreen.Lobby.name) {
                composable(PokerDiceScreen.Lobby.name) { }
                composable(PokerDiceScreen.Lobbies.name) { }
                composable("Match/{matchId}") { }
            }

            navController.graph = graph

            lobbyDetailsViewModel.fetchLobbyById(4)

            LobbyDetailsScreen(
                onNavigateToLobbies = { navController.navigate(PokerDiceScreen.Lobbies.name) },
                lobbyId = 4,
                lobbyDetailsViewModel = lobbyDetailsViewModel,
            )
        }
    }

    @Test
    fun confirm_LobbyDetailsScreen_Exists() {
        composeTestRule.onNodeWithText("My Test Lobby").assertExists()
        composeTestRule.onNodeWithTag(LOBBY_DESCRIPTION_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOBBY_PLAYERS_INFO_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOBBY_ROUNDS_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOBBY_PLAYERS_TITLE_TAG).assertExists()
        composeTestRule.onNodeWithTag("LobbyPlayer_1000").assertExists() // Alice
        composeTestRule.onNodeWithTag("LobbyPlayer_1001").assertExists() // Bob
        composeTestRule.onNodeWithTag("LobbyPlayer_1002").assertExists() // Charlie
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(START_MATCH_BUTTON_TAG).assertExists().assertIsEnabled()
    }


    @Test
    fun clicking_back_arrow_triggers_navigate_to_LobbyScreen() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).performClick()
        assertEquals(
            PokerDiceScreen.Lobbies.name,
            navController.currentBackStackEntry?.destination?.route
        )
    }
}