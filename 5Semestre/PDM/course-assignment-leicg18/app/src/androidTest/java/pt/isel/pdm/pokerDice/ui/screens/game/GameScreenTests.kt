package pt.isel.pdm.pokerDice.ui.screens.game

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.Round
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.main.MATCH_TAG
import pt.isel.pdm.pokerDice.main.PokerDiceScreen
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.MatchServices
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.MatchViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock


/*
    * Nota: ter em consideração que são testes apenas de interface gráfica, pelo que os testes podem não seguir exatamente a
    * lógica do jogo.
    * São testes apenas para testar a gameScreen, não para testar a lógica do jogo.
 */
class GameScreenTests {
    @get:Rule
    val composeTestRule = createComposeRule()


    lateinit var navController: TestNavHostController
    lateinit var matchViewModel: MatchViewModel

    val trxManager = TransactionManagerInMem()
    val passwordEncoder = SimpleSha256PasswordEnconder()
    val tokenEncoder = SimpleTokenEncoder()
    val userConfig = UsersDomainConfig()
    val clock = Clock.systemUTC()
    val authentication = FakeAuthInfoRepo()
    val userServices = UserServices(passwordEncoder, tokenEncoder, userConfig, trxManager, clock)

    @Before
    fun init() {
        matchViewModel = MatchViewModel(
            matchServices = MatchServices(trxManager),
            userServices = userServices,
            authInfo = authentication,
            lobbyServices = LobbyServices(trxManager)
        )
    }

    private fun setContentWithMatch(match: Match, round: Round, currentUser: User) {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).also {
                it.navigatorProvider.addNavigator(ComposeNavigator())
                it.graph = it.createGraph(startDestination = MATCH_TAG) {
                    composable(MATCH_TAG) { }
                    composable(PokerDiceScreen.Lobbies.name) { }
                }
            }
            GameScreen(
                matchId = match.id,
                viewModel = matchViewModel,
                matchPreview = match,
                currRound = round,
                currUserIdPreview = currentUser.id,
                onNavigateToLobbies = { navController.navigate(PokerDiceScreen.Lobbies.name) }
            )
        }
    }

    private fun setContentLoading() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).also {
                it.navigatorProvider.addNavigator(ComposeNavigator())
                it.graph = it.createGraph(startDestination = MATCH_TAG) {
                    composable(MATCH_TAG) { }
                    composable(PokerDiceScreen.Lobbies.name) { }
                }
            }
            GameScreen(
                matchId = 1,
                viewModel = matchViewModel,
                onNavigateToLobbies = { navController.navigate(PokerDiceScreen.Lobbies.name) }
            )
        }
    }

    @Test
    fun confirm_GameScreen_Match_Null_Exists() {
        setContentLoading()
        composeTestRule.onNodeWithTag(GAME_LOADING_TAG).assertIsDisplayed()
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    fun confirm_GameScreen_Match_NotNull_Exists_With_No_Plays() {
        setContentWithMatch(matchNoPlay, roundNoPlay, alice)
        composeTestRule.onNodeWithTag("PLAYER_${alice.id}_STATUS")
            .assertExists()
        composeTestRule.onNodeWithTag("PLAYER_${alice.id}_HAND")
            .assertExists()
        composeTestRule.onNodeWithTag("PLAYER_${bob.id}_STATUS")
            .assertExists()
        composeTestRule.onNodeWithTag("PLAYER_${bob.id}_HAND")
            .assertExists()
        composeTestRule.onNodeWithText("9=NINE, T=TEN, J=JACK, Q=QUEEN, K=KING, A=ACE")
            .assertExists()
        composeTestRule.onAllNodesWithTag(IMAGE_TAG).assertCountEquals(0)
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG)
            .assertExists()
            .assertIsEnabled()
        composeTestRule.onNode(
            hasTestTag(REROLL_TEXT_TAG),
            useUnmergedTree = true
        ).assertExists()
        composeTestRule.onNodeWithTag(SKIP_BUTTON_TAG).assertExists().assertIsNotEnabled()
        composeTestRule.onNodeWithTag(REROLL_COUNT_TAG).assertExists()
    }

    // Teste considera que o jogador "Alice" já fez um roll
    @Test
    fun gameScreen_After_One_Reroll() {
        setContentWithMatch(match, round, alice)
        composeTestRule.onNodeWithTag(REROLL_BUTTON_TAG).assertExists().assertIsNotEnabled()
        composeTestRule.onNodeWithTag(SKIP_BUTTON_TAG).assertExists().assertIsEnabled()
        val images = composeTestRule.onAllNodesWithTag(IMAGE_TAG)
        images.assertCountEquals(5)
        //confirmar que as imagens são clicaveis e ninguém está selecionado
        for (i in 0..<5) {
            images[i].assertExists().assertIsSelectable().assertIsNotSelected()
        }
        composeTestRule.onNodeWithTag("PLAYER_${alice.id}_STATUS")
            .assertExists()
        composeTestRule.onNodeWithTag("PLAYER_${alice.id}_HAND")
            .assertExists()
        composeTestRule.onNodeWithTag("PLAYER_${bob.id}_STATUS")
            .assertExists()
        composeTestRule.onNodeWithTag("PLAYER_${bob.id}_HAND")
            .assertExists()
        composeTestRule.onNodeWithText("9=NINE, T=TEN, J=JACK, Q=QUEEN, K=KING, A=ACE")
            .assertExists() //hasn't change
        composeTestRule.onNodeWithTag(SCORE_TEXT_TAG)
            .assertExists()
            .assertTextContains("High Card", substring = true)
        composeTestRule.onNodeWithTag(REROLL_COUNT_TAG)
            .assertExists()
            .assertTextContains("1/3", substring = true)
    }

    @Test
    fun user_Playing_Changes() {
        setContentWithMatch(matchWaiting, waitingRound, alice)
        composeTestRule.onNodeWithTag("PLAYER_${alice.id}_STATUS")
            .assertExists()
        composeTestRule.onNodeWithTag("PLAYER_${alice.id}_HAND")
            .assertExists()
            .assertTextContains("9TJQK  [High Card]", substring = true)
        composeTestRule.onNodeWithTag("PLAYER_${bob.id}_STATUS")
            .assertExists()
        composeTestRule.onNodeWithTag("PLAYER_${bob.id}_HAND")
            .assertExists()
            .assertTextContains("9TJQK  [Score: N/A]", substring = true)
        composeTestRule.onNodeWithText("9=NINE, T=TEN, J=JACK, Q=QUEEN, K=KING, A=ACE")
            .assertExists() //haven't change
        composeTestRule.onNodeWithTag(WAITING_FOR_TURN_TAG).assertExists()
    }

    @Test
    fun confirm_Winner_Displayed_Correctly() {
        setContentWithMatch(matchWinnerInProgress, roundWithWinner, alice)
        composeTestRule.onNodeWithTag("PLAYER_${alice.id}_STATUS")
            .assertExists()
        composeTestRule.onNodeWithTag("PLAYER_${bob.id}_STATUS")
            .assertExists()
        composeTestRule.onNodeWithTag(SCORE_TEXT_TAG)
            .assertExists()
            .assertTextContains("Five of a Kind", substring = true)
    }

    @Test
    fun confirm_Winner_Display() {
        setContentWithMatch(completedMatch, finalRound, alice)
        composeTestRule.onNodeWithTag("ROUND_${finalRound.id}_NUMBER")
            .assertExists()
            .assertTextContains("1", substring = true)

        finalRound.turns.forEach { turn ->
            composeTestRule.onNodeWithTag("ROUND_${finalRound.id}_PLAYER_${turn.player.id}_NAME")
                .assertExists()
                .assertTextContains(turn.player.name, substring = true)

            composeTestRule.onNodeWithTag("ROUND_${finalRound.id}_PLAYER_${turn.player.id}_HAND")
                .assertExists()
                .assertTextContains(turn.handSummary(), substring = true)
        }

        composeTestRule.onNodeWithTag("ROUND_${finalRound.id}_WINNER")
            .assertExists()
            .assertTextContains(alice.name, substring = true)

        composeTestRule.onNodeWithTag("MATCH_WINNER")
            .assertExists()
            .assertTextContains(alice.name, substring = true)

        composeTestRule.onNodeWithTag(BACK_TO_LOBBIES_TAG).assertExists().assertIsEnabled()
    }

    @Test
    fun confirm_BackToLobbies_Navigation() {
        setContentWithMatch(completedMatch, finalRound, alice)
        composeTestRule.onNodeWithTag(BACK_TO_LOBBIES_TAG).performClick()
        val route = navController.currentDestination?.route
        assertEquals(route, PokerDiceScreen.Lobbies.name)
    }
}