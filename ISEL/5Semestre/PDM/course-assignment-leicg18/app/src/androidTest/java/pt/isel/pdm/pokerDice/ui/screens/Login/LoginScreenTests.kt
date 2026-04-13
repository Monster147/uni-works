package pt.isel.pdm.pokerDice.ui.screens.Login

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.main.PokerDiceScreen
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.ui.screens.login_register.LOGIN_BUTTON_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.LOGIN_EMAIL_INPUT_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.LOGIN_PASSWORD_INPUT_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.LoginScreen
import pt.isel.pdm.pokerDice.ui.screens.login_register.OR_DIVIDER_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.REGISTER_BUTTON_TAG
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

class LoginScreenTests {
    @get:Rule
    val composeTestRule = createComposeRule()

    lateinit var navController: TestNavHostController
    lateinit var loginViewModel: LoginViewModel

    val trxManager = TransactionManagerInMem()
    val passwordEncoder = SimpleSha256PasswordEnconder()
    val tokenEncoder = SimpleTokenEncoder()
    val userConfig = UsersDomainConfig()
    val clock = Clock.systemUTC()
    val authentication = FakeAuthInfoRepo()

    @Before
    fun setup() {
        loginViewModel = LoginViewModel(
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

            val graph = navController.createGraph(startDestination = PokerDiceScreen.Login.name) {
                composable(PokerDiceScreen.Login.name) { }
                composable(PokerDiceScreen.Title.name) { }
                composable(PokerDiceScreen.Register.name) { }
            }
            navController.graph = graph

            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToTitle = { navController.navigate(PokerDiceScreen.Title.name) },
                onNavigateToRegister = { navController.navigate(PokerDiceScreen.Register.name) }
            )
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun confirm_LoginScreen_Exists() {
        composeTestRule.onAllNodesWithText("Login").assertCountEquals(2)
        composeTestRule.onNodeWithTag(LOGIN_EMAIL_INPUT_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOGIN_PASSWORD_INPUT_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertExists().assertIsNotEnabled()
        composeTestRule.onNodeWithTag(OR_DIVIDER_TAG).assertExists()
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists()
    }

    @Test
    fun successful_Login_Triggers_Navigate_To_TitleScreen() {
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onAllNodesWithText("Login").assertCountEquals(2)
        composeTestRule.onNodeWithTag(LOGIN_EMAIL_INPUT_TAG)
            .performTextInput("johndoe@gmail.com")
        composeTestRule.onNodeWithTag(LOGIN_PASSWORD_INPUT_TAG)
            .performTextInput("Hash@123")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsEnabled().performClick()

        composeTestRule.waitForIdle()

        val route = navController.currentBackStackEntry?.destination?.route

        Assert.assertEquals(PokerDiceScreen.Title.name, route)
    }

    @Test
    fun navigate_To_RegisterScreen_On_RegisterButton_Click() {
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).performClick()

        composeTestRule.waitForIdle()
        val route = navController.currentBackStackEntry?.destination?.route
        Assert.assertEquals(PokerDiceScreen.Register.name, route)
    }

    @Test
    fun unsuccessful_Login_DueTo_Wrong_Email() {
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(LOGIN_EMAIL_INPUT_TAG)
            .performTextInput("a@gmail.com")
        composeTestRule.onNodeWithTag(LOGIN_PASSWORD_INPUT_TAG)
            .performTextInput("Hash@123")
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsEnabled().performClick()

        composeTestRule.waitForIdle()

        Assert.assertEquals(
            PokerDiceScreen.Login.name,
            navController.currentBackStackEntry?.destination?.route
        )
    }

    @Test
    fun unsuccessful_Login_DueTo_Wrong_Pass() {
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(LOGIN_EMAIL_INPUT_TAG)
            .performTextInput("johndoe@gmail.com")
        composeTestRule.onNodeWithTag(LOGIN_PASSWORD_INPUT_TAG)
            .performTextInput("pdM@123456")
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsEnabled().performClick()

        composeTestRule.waitForIdle()

        Assert.assertEquals(
            PokerDiceScreen.Login.name,
            navController.currentBackStackEntry?.destination?.route
        )
    }

    @Test
    fun login_Button_Doenst_Show_Missing_Email() {
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(LOGIN_EMAIL_INPUT_TAG)
            .performTextInput("")
        composeTestRule.onNodeWithTag(LOGIN_PASSWORD_INPUT_TAG)
            .performTextInput("pdm")
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsNotEnabled().performClick()
        Assert.assertEquals(
            PokerDiceScreen.Login.name,
            navController.currentBackStackEntry?.destination?.route
        )
    }

    @Test
    fun login_Button_Doenst_Show_Missing_Password() {
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(LOGIN_EMAIL_INPUT_TAG)
            .performTextInput("something@gmail.com")
        composeTestRule.onNodeWithTag(LOGIN_PASSWORD_INPUT_TAG)
            .performTextInput("")
        composeTestRule.onNodeWithTag(LOGIN_BUTTON_TAG).assertIsNotEnabled().performClick()
        Assert.assertEquals(
            PokerDiceScreen.Login.name,
            navController.currentBackStackEntry?.destination?.route
        )
    }
}