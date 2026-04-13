package pt.isel.pdm.pokerDice.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
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
import pt.isel.pdm.pokerDice.main.PokerDiceScreen
import pt.isel.pdm.pokerDice.ui.INFO_BUTTON_TAG
import pt.isel.pdm.pokerDice.ui.PROFILE_BUTTON_TAG


class TitleScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val graph = navController.createGraph(startDestination = PokerDiceScreen.Title.name) {
                composable(PokerDiceScreen.Title.name) { }
                composable(PokerDiceScreen.Profile.name) { }
                composable(PokerDiceScreen.About.name) { }
                composable(PokerDiceScreen.Lobbies.name) { }
            }
            navController.graph = graph

            TitleScreen(
                onNavigateToAbout = { navController.navigate(PokerDiceScreen.About.name) },
                onNavigateToLobbies = { navController.navigate(PokerDiceScreen.Lobbies.name) },
                onNavigateToProfile = { navController.navigate(PokerDiceScreen.Profile.name) },
            )
        }
    }

    @Test
    fun confirm_TitleScreen_Exists() {
        composeTestRule.onNodeWithTag(PROFILE_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(INFO_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithText("Poker Dice").assertExists()
        composeTestRule.onNodeWithTag(LOBBY_BUTTON_TAG).assertExists()
    }

    @Test
    fun titleScreen_displays_buttons_and_title() {
        composeTestRule.onNodeWithText("Poker Dice").assertExists()
        composeTestRule.onNodeWithTag(LOBBY_BUTTON_TAG).assertExists()
    }

    @Test
    fun clicking_LobbiesButton_triggersNavigateToLobbies() {
        composeTestRule.onNodeWithTag(LOBBY_BUTTON_TAG).performClick()
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(PokerDiceScreen.Lobbies.name, route)
    }

    @Test
    fun clicking_InfoButton_triggersNavigateToAbout() {
        composeTestRule.onNodeWithTag(INFO_BUTTON_TAG).performClick()
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(PokerDiceScreen.About.name, route)
    }

    @Test
    fun clicking_ProfileButton_triggersNavigateToProfile() {
        composeTestRule.onNodeWithTag(PROFILE_BUTTON_TAG).performClick()
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(PokerDiceScreen.Profile.name, route)
    }
}