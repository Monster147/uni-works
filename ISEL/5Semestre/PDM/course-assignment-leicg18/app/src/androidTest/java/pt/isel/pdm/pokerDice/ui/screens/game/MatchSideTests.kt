package pt.isel.pdm.pokerDice.ui.screens.game

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.Dice
import pt.isel.pdm.pokerDice.domain.DiceFace
import pt.isel.pdm.pokerDice.domain.Hand
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Turn
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.main.MATCH_TAG

class MatchSideTests {

    //Nota, como não existe um ViewModel a gerir o estado, consideramos que o botao de reroll foi pressionado, mas não contamos este clique para os nenhum dos testes

    @get:Rule
    val composeTestRule = createComposeRule()

    lateinit var navController: TestNavHostController
    val player = User(
        id = 1,
        name = "Ivanovic",
        email = "0pontis@gmail.com",
        passwordValidation = PasswordValidationInfo("hash")
    )
    val hand = Hand(
        listOf(
            Dice(DiceFace.ACE),
            Dice(DiceFace.ACE),
            Dice(DiceFace.KING),
            Dice(DiceFace.QUEEN),
            Dice(DiceFace.JACK)
        )
    )
    val turn = Turn(
        id = 1,
        roundId = 1,
        player = player,
        hand = hand,
    )
    val turnNoHand = Turn(
        id = 1,
        roundId = 1,
        player = player,
        hand = Hand(emptyList()),
    )
    var isSkipped = false

    @Before
    fun resetFlags() {
        isSkipped = false
    }

    private fun setContentWithHand() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            navController.graph = navController.createGraph(startDestination = MATCH_TAG) {
                composable(MATCH_TAG) {}
            }
            MatchSide(turn = turn, onSkip = { isSkipped = true })
        }
    }

    private fun setContentWithoutHand() {
        composeTestRule.setContent {
            MatchSide(turn = turnNoHand, onSkip = { isSkipped = true })
        }
    }

    @Test
    fun confirm_MatchScreen_Exists_FirstRoll() {
        setContentWithoutHand()
        composeTestRule.onNodeWithTag(YOUR_TURN_TAG).assertExists()
        composeTestRule.onNodeWithTag(IMAGE_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SKIP_BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().assertIsEnabled()
    }

    @Test
    fun confirm_MatchScreen_Exists_After_FirstRoll() {
        setContentWithHand()
        composeTestRule.onAllNodesWithTag(IMAGE_TAG).assertCountEquals(5)
        composeTestRule.onNodeWithTag(SKIP_BUTTON_TAG).assertExists().assertIsEnabled()
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun verify_If_The_Images_Can_Be_Selected() {
        setContentWithHand()
        val images = composeTestRule.onAllNodesWithTag(IMAGE_TAG)
        images.assertCountEquals(5)
        images[0].assertExists().performClick()
        images[1].assertExists().performClick()
        images[2].assertExists().performClick()
        images[3].assertExists()
        images[4].assertExists().performClick()
        images[0].assertExists().performClick()
        images[0].assertIsSelectable().assertIsNotSelected()
        images[1].assertIsSelectable().assertIsSelected()
        images[2].assertIsSelectable().assertIsSelected()
        images[3].assertIsSelectable().assertIsNotSelected()
        images[4].assertIsSelectable().assertIsSelected()
    }

    @Test
    fun verify_RerollCount_Changing() {
        setContentWithHand()
        val images = composeTestRule.onAllNodesWithTag(IMAGE_TAG)
        images.assertCountEquals(5)
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().performClick()
        for (i in 0..<5) {
            images[i].assertExists().assertIsSelectable().assertIsNotSelected()
        }
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().assertIsNotEnabled()
        images[0].assertExists().assertIsSelectable().assertIsNotSelected().performClick()
            .assertIsSelected()
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().assertIsEnabled()
            .performClick()
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().assertIsNotEnabled()
        for (i in 0..<5) {
            images[i].assertExists().assertIsSelectable().assertIsNotSelected()
        }
        images[0].assertExists().assertIsSelectable().assertIsNotSelected().performClick()
            .assertIsSelected()
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().assertIsEnabled()
            .performClick()
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun verify_Skip_Button_Is_Active_When_Is_Not_FirstRoll() {
        setContentWithoutHand()
        composeTestRule.onNodeWithTag(SKIP_BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().performClick()
        composeTestRule.onNodeWithTag(SKIP_BUTTON_TAG).assertIsEnabled().performClick()
        assert(isSkipped)
    }
}