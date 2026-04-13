package pt.isel.pdm.pokerDice.ui.screens.lobby

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.main.PokerDiceScreen
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.LobbyServiceInterface
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.MatchServices
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.ui.BACK_BUTTON_TAG
import pt.isel.pdm.pokerDice.ui.INFO_BUTTON_TAG
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

class LobbyScreenTests {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var fakeLobbyService: LobbyServiceInterface
    lateinit var navController: TestNavHostController
    lateinit var lobbyViewModel: LobbyViewModel

    val authentication = FakeAuthInfoRepo()

    @Before
    fun setupAppNavHost() {
        val trxManager = TransactionManagerInMem()
        val passwordEncoder = SimpleSha256PasswordEnconder()
        val tokenEncoder = SimpleTokenEncoder()
        val userConfig = UsersDomainConfig()
        val clock = Clock.systemUTC()
        fakeLobbyService = LobbyServices(TransactionManagerInMem())
        lobbyViewModel = LobbyViewModel(
            fakeLobbyService, UserServices(
                passwordEncoder,
                tokenEncoder,
                userConfig,
                trxManager,
                clock
            ),
            authentication
        )
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val graph = navController.createGraph(startDestination = PokerDiceScreen.Lobbies.name) {
                composable(PokerDiceScreen.Lobbies.name) { }
                composable(PokerDiceScreen.Lobby.name) { }
                composable(PokerDiceScreen.LobbyCreation.name) { }
                composable(PokerDiceScreen.Title.name) { }
            }

            navController.graph = graph

            LobbyScreen(
                onNavigateToLobbyDetails = { navController.navigate(PokerDiceScreen.Lobby.name) },
                onNavigateToLobbyCreation = { navController.navigate(PokerDiceScreen.LobbyCreation.name) },
                onNavigateToTitle = { navController.navigate(PokerDiceScreen.Title.name) },
                viewModel = lobbyViewModel
            )
        }
    }

    @Test
    fun lobbyScreen_has_everything_that_is_suppose_to() {
        composeTestRule.onNodeWithTag(testTag = BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(testTag = INFO_BUTTON_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(testTag = CREATE_LOBBY_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithText("First Lobby").assertExists()
        composeTestRule.onNodeWithTag("PlayersText_1").assertExists()
        composeTestRule.onNodeWithTag("AnteText_1").assertExists()
        composeTestRule.onNodeWithText("Second Lobby").assertExists()
        composeTestRule.onNodeWithTag("PlayersText_2").assertExists()
        composeTestRule.onNodeWithTag("AnteText_2").assertExists()
        composeTestRule.onNodeWithText("Third Lobby").assertExists()
        composeTestRule.onNodeWithTag("PlayersText_3").assertExists()
        composeTestRule.onNodeWithTag("AnteText_3").assertExists()
        composeTestRule.onNodeWithText("My Test Lobby").assertExists()
        composeTestRule.onNodeWithTag("PlayersText_4").assertExists()
        composeTestRule.onNodeWithTag("AnteText_4").assertExists()
        val detailButtons = composeTestRule.onAllNodesWithTag(LOBBY_DETAILS_BUTTON_TAG)
        for (i in 0..<4) {
            detailButtons[i].assertExists()
        }
        detailButtons.assertCountEquals(4)
    }

    @Test
    fun clicking_back_arrow_triggers_navigate_to_titleScreen() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).performClick()
        assertEquals(
            PokerDiceScreen.Title.name,
            navController.currentBackStackEntry?.destination?.route
        )
    }

    @Test
    fun clicking_details_button_triggers_navigate_to_lobbyDetailsScreen() {
        val detailButtons = composeTestRule.onAllNodesWithTag(LOBBY_DETAILS_BUTTON_TAG)
        detailButtons.assertCountEquals(4)
        detailButtons[0].performClick()
        assertEquals(
            PokerDiceScreen.Lobby.name,
            navController.currentBackStackEntry?.destination?.route
        )
    }

    @Test
    fun clicking_create_button_triggers_navigate_to_lobbyCreationScreen() {
        composeTestRule.onNodeWithTag(testTag = CREATE_LOBBY_BUTTON_TAG).performClick()
        assertEquals(
            PokerDiceScreen.LobbyCreation.name,
            navController.currentBackStackEntry?.destination?.route
        )
    }

}
