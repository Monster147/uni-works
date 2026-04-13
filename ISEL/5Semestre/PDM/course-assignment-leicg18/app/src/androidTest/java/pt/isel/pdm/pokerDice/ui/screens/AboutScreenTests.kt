package pt.isel.pdm.pokerDice.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pt.isel.pdm.pokerDice.ui.BACK_BUTTON_TAG

@RunWith(AndroidJUnit4::class)
class AboutScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    val rules = "Poker Dice is a simple dice game where 5 dice marked with card values (9, 10, J, Q, K, A) are rolled to form poker-style hands. Players may re-roll dice up to three times to aim for the best hand, such as Five of a Kind, Full House, or a Straight. The winner is the player with the highest-ranking combination.\n" +
            "For more details information click on rules."

    @Test
    fun aboutScreen_exists(){
        composeTestRule.setContent {
            AboutScreen()
        }

        composeTestRule.onNodeWithText(text = rules).assertExists()
        composeTestRule.onNodeWithTag(testTag = BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(testTag = RULES_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(testTag = GMAIL_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(testTag = DEVELOPERS_TEXT_TAG).assertExists()
    }

    @Test
    fun clicking_WebsiteButton_triggersOnRulesClick() {
        var rulesClicked = false
        composeTestRule.setContent {
            AboutScreen(
                onRulesClick = { rulesClicked = true }
            )
        }
        composeTestRule.onNodeWithTag(testTag = RULES_BUTTON_TAG).performClick()
        assert(rulesClicked)
    }

    @Test
    fun clicking_BackArrow_triggersOnNavigateToTitle() {
        var backCLicked = false
        composeTestRule.setContent {
            AboutScreen(
                onNavigateToTitle = { backCLicked = true }
            )
        }
        composeTestRule.onNodeWithTag(testTag = BACK_BUTTON_TAG).performClick()
        assert(backCLicked)
    }

    @Test
    fun clicking_GmailButton_triggersOnGmailClick() {
        var gmailClicked = false
        composeTestRule.setContent {
            AboutScreen(
                onGmailClick = { gmailClicked = true }
            )
        }
        composeTestRule.onNodeWithTag(testTag = GMAIL_BUTTON_TAG).performClick()
        assert(gmailClicked)
    }
}
