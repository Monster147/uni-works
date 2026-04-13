package pt.isel.pdm.pokerDice.ui.screens.lobby

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.ui.BACK_BUTTON_TAG
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

class LobbyCreationScreenTests {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var fakeLobbyService: LobbyServiceInterface
    private lateinit var lobbyCreationViewModel: LobbyCreationViewModel
    lateinit var navController: TestNavHostController
    val trxManager = TransactionManagerInMem()
    val passwordEncoder = SimpleSha256PasswordEnconder()
    val tokenEncoder = SimpleTokenEncoder()
    val userConfig = UsersDomainConfig()
    val clock = Clock.systemUTC()
    val authentication = FakeAuthInfoRepo()

    @Before
    fun setupAppNavHost() {
        fakeLobbyService = LobbyServices(TransactionManagerInMem())
        lobbyCreationViewModel = LobbyCreationViewModel(
            fakeLobbyService,
            UserServices(
                passwordEncoder,
                tokenEncoder,
                userConfig,
                trxManager,
                clock
            ), authentication
        )
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val graph =
                navController.createGraph(startDestination = PokerDiceScreen.LobbyCreation.name) {
                    composable(PokerDiceScreen.LobbyCreation.name) { }
                    composable(PokerDiceScreen.Lobbies.name) { }
                    composable("LobbyDetails/{lobbyName}") { }
                }
            navController.graph = graph

            LobbyCreationScreen(
                viewModel = lobbyCreationViewModel,
                onNavigateToLobbies = { navController.navigate(PokerDiceScreen.Lobbies.name) },
                onNavigateToLobbyDetails = { lobbyName ->
                    navController.navigate("LobbyDetails/$lobbyName")
                }
            )
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun lobbyCreationScreen_exists() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOBBY_NAME_INPUT_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOBBY_DESCRIPTION_INPUT_TAG).assertExists()
        composeTestRule.onNodeWithTag(PLAYERS_NUMBER_SELECT_TAG).assertExists()
        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertExists().assertIsNotEnabled()
        composeTestRule.onNodeWithTag(SELECT_NUMBER_PLAYERS_FIRST_TAG).assertExists()
    }

    @Test
    fun clicking_CreateLobbyButton_triggersNavigateToLobbies() {

        composeTestRule.onNodeWithTag(LOBBY_NAME_INPUT_TAG)
            .performTextInput("My Test Lobby 2")

        composeTestRule.onNodeWithTag(LOBBY_DESCRIPTION_INPUT_TAG)
            .performTextInput("A fun lobby for testing")

        composeTestRule.onNodeWithTag(PLAYERS_NUMBER_SELECT_TAG).performClick()
        composeTestRule.onNodeWithText("2").performClick()

        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertExists().performClick()
        composeTestRule.onNodeWithText("4").performClick()

        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertExists().performClick()
        composeTestRule.onNodeWithText("20").performClick()

        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG)
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()

        val route = navController.currentBackStackEntry?.destination?.route
        val arg = navController.currentBackStackEntry?.arguments?.getString("lobbyName")

        assertEquals("LobbyDetails/{lobbyName}", route)
        assertEquals("5", arg) //ja existem 4 lobbies em memoria
    }

    @Test
    fun lobbyCreation_button_disabled_when_all_fields_empty() {
        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun lobbyCreation_button_disabled_when_only_name_field_not_empty() {
        composeTestRule.onNodeWithTag(LOBBY_NAME_INPUT_TAG)
            .performTextInput("My Test Lobby")
        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun lobbyCreation_button_disabled_when_only_description_field_not_empty() {
        composeTestRule.onNodeWithTag(LOBBY_DESCRIPTION_INPUT_TAG)
            .performTextInput("A fun lobby for testing")
        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun lobbyCreation_button_disabled_when_only_number_of_players_field_not_empty() {
        composeTestRule.onNodeWithTag(PLAYERS_NUMBER_SELECT_TAG).performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertExists()
        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun lobbyCreation_button_disabled_when_name_and_description_fields_not_empty() {
        composeTestRule.onNodeWithTag(LOBBY_NAME_INPUT_TAG)
            .performTextInput("My Test Lobby")
        composeTestRule.onNodeWithTag(LOBBY_DESCRIPTION_INPUT_TAG)
            .performTextInput("A fun lobby for testing")
        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun lobbyCreation_button_disabled_when_name_and_number_of_players_fields_not_empty() {
        composeTestRule.onNodeWithTag(LOBBY_NAME_INPUT_TAG)
            .performTextInput("My Test Lobby")
        composeTestRule.onNodeWithTag(PLAYERS_NUMBER_SELECT_TAG).performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertExists()
        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun lobbyCreation_button_disabled_when_description_and_number_of_players_fields_not_empty() {
        composeTestRule.onNodeWithTag(LOBBY_DESCRIPTION_INPUT_TAG)
            .performTextInput("A fun lobby for testing")
        composeTestRule.onNodeWithTag(PLAYERS_NUMBER_SELECT_TAG).performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertExists()
        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun lobbyCreation_button_disabled_when_only_number_of_rounds_empty() {
        composeTestRule.onNodeWithTag(LOBBY_NAME_INPUT_TAG)
            .performTextInput("My Test Lobby")
        composeTestRule.onNodeWithTag(LOBBY_DESCRIPTION_INPUT_TAG)
            .performTextInput("A fun lobby for testing")
        composeTestRule.onNodeWithTag(PLAYERS_NUMBER_SELECT_TAG).performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertExists()
        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun lobbyCreation_button_disabled_when_all_fields_filled_except_ante() {
        composeTestRule.onNodeWithTag(LOBBY_NAME_INPUT_TAG)
            .performTextInput("My Test Lobby")
        composeTestRule.onNodeWithTag(LOBBY_DESCRIPTION_INPUT_TAG)
            .performTextInput("A fun lobby for testing")

        composeTestRule.onNodeWithTag(PLAYERS_NUMBER_SELECT_TAG).performClick()
        composeTestRule.onNodeWithText("2").performClick()

        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertExists().performClick()
        composeTestRule.onNodeWithText("4").performClick()

        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun lobbyCreation_button_enabled_when_all_fields_filled_including_ante() {
        composeTestRule.onNodeWithTag(LOBBY_NAME_INPUT_TAG)
            .performTextInput("My Test Lobby")
        composeTestRule.onNodeWithTag(LOBBY_DESCRIPTION_INPUT_TAG)
            .performTextInput("A fun lobby for testing")

        composeTestRule.onNodeWithTag(PLAYERS_NUMBER_SELECT_TAG).performClick()
        composeTestRule.onNodeWithText("2").performClick()

        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertExists().performClick()
        composeTestRule.onNodeWithText("4").performClick()

        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertExists().performClick()
        composeTestRule.onNodeWithText("20").performClick()

        composeTestRule.onNodeWithTag(LOBBIES_BUTTON_TAG).assertIsEnabled()
    }

    @Test
    fun lobbyCreation_ante_selection_shows_correct_values() {
        composeTestRule.onNodeWithTag(PLAYERS_NUMBER_SELECT_TAG).performClick()
        composeTestRule.onNodeWithText("2").performClick()

        composeTestRule.onNodeWithTag(ROUNDS_NUMBER_SELECT_TAG).assertExists().performClick()
        composeTestRule.onNodeWithText("4").performClick()

        composeTestRule.onNodeWithTag(ANTE_SELECT_TAG).assertExists().performClick()

        composeTestRule.onNodeWithText("10").assertExists()
        composeTestRule.onNodeWithText("20").assertExists()
        composeTestRule.onNodeWithText("30").assertExists()
    }
}