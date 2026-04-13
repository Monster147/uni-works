package pt.isel.pdm.pokerDice.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.main.PokerDiceScreen
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.InvitationServices
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.UserViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfo
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

class ProfileScreenTestsLoggedIn {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeUserService: UserServiceInterface
    private lateinit var fakeInvitationServices: InvitationServices
    lateinit var navController: TestNavHostController
    lateinit var userViewModel: UserViewModel

    val trxManager = TransactionManagerInMem()
    val passwordEncoder = SimpleSha256PasswordEnconder()
    val tokenEncoder = SimpleTokenEncoder()
    val userConfig = UsersDomainConfig()
    val clock = Clock.systemUTC()
    val authentication = FakeAuthInfoRepo()

    @Before
    fun setupAppNavHost() = runBlocking {

        fakeUserService =  UserServices(
            passwordEncoder,
            tokenEncoder,
            userConfig,
            trxManager,
            clock
        )

        fakeInvitationServices = InvitationServices(
            trxManager
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

        userViewModel = UserViewModel(
            fakeUserService,
            authentication,
            fakeInvitationServices
        )

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())


            val graph = navController.createGraph(startDestination = PokerDiceScreen.Profile.name) {
                composable(PokerDiceScreen.Profile.name) { }
                composable(PokerDiceScreen.Title.name) { }
                composable(PokerDiceScreen.Login.name) { }
            }

            navController.graph = graph

            ProfileScreen(
                onNavigateToTitle = { navController.navigate(PokerDiceScreen.Title.name) },
                userViewModel = userViewModel,
                onLogout = { navController.navigate(PokerDiceScreen.Login.name) }
            )
        }
    }

    @Test
    fun testProfileScreen_DisplaysUserInfo() {
        composeTestRule.onNodeWithTag(PROFILE_NAME_TAG)
            .assertExists()
            .assertTextContains("Alice", substring = true)
        composeTestRule.onNodeWithTag(PROFILE_EMAIL_TAG)
            .assertExists()
            .assertTextContains("alice@gmail.com", substring = true)
        composeTestRule.onNodeWithTag(PROFILE_BALANCE_TAG)
            .assertExists()
            .assertTextContains("500", substring = true)
        composeTestRule.onNodeWithTag(STATS_TAG).assertExists()
        composeTestRule.onNodeWithTag(ROUNDS_WON_TAG).assertExists()
        composeTestRule.onNodeWithTag(ROUNDS_LOST_TAG).assertExists()
        composeTestRule.onNodeWithTag(ROUNDS_DRAW_TAG).assertExists()
        composeTestRule.onNodeWithTag(TOTAL_MATCHES_TAG).assertExists()
        composeTestRule.onNodeWithTag(MATCHES_WON_TAG).assertExists()
        composeTestRule.onNodeWithTag(MATCHES_LOST_TAG).assertExists()
        composeTestRule.onNodeWithTag(MATCHES_DRAW_TAG).assertExists()
        composeTestRule.onNodeWithTag(WIN_RATE_TAG).assertExists()
        composeTestRule.onNodeWithTag(WIN_RATE_CIRCLE_TAG).assertExists()
        composeTestRule.onNodeWithTag(GET_CODE_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOGOUT_BUTTON_TAG).assertExists()
    }

    @Test
    fun testProfileScreen_clickingCodeGetsNewCode() {
        composeTestRule.onNodeWithTag(GET_CODE_BUTTON_TAG).assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(CODE_TEXT_TAG).assertExists()
        composeTestRule.onNodeWithTag(VALID_CODE_TAG).assertExists()
        composeTestRule.onNodeWithTag(VALIDATE_CODE_BUTTON_TAG).assertExists()
    }

    @Test
    fun testProfileScreen_clickingLogoutNavigatesToLoginScreen() {
        composeTestRule.onNodeWithTag(LOGOUT_BUTTON_TAG).assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        assert(navController.currentBackStackEntry?.destination?.route == PokerDiceScreen.Login.name)
    }
}