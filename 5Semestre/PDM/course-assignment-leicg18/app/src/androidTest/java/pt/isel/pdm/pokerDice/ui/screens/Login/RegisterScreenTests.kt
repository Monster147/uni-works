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
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.main.PokerDiceScreen
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.ui.BACK_BUTTON_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.CREATE_EMAIL_INPUT_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.CREATE_NAME_INPUT_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.CREATE_PASSWORD_INPUT_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.INVITATION_CODE_INPUT_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.PASSWORD_REQUIREMENTS_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.REGISTER_BUTTON_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.REGISTER_BUTTON_TEXT_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.REGISTER_TITLE_TAG
import pt.isel.pdm.pokerDice.ui.screens.login_register.RegisterScreen
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.RegisterViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

class RegisterScreenTests {
    @get:Rule
    val composeTestRule = createComposeRule()

    lateinit var navController: TestNavHostController
    lateinit var registerViewModel: RegisterViewModel
    lateinit var loginViewModel: LoginViewModel

    val trxManager = TransactionManagerInMem()
    val passwordEncoder = SimpleSha256PasswordEnconder()
    val tokenEncoder = SimpleTokenEncoder()
    val userConfig = UsersDomainConfig()
    val clock = Clock.systemUTC()
    val authentication = FakeAuthInfoRepo()
    val userServices = UserServices(passwordEncoder, tokenEncoder, userConfig, trxManager, clock)

    @Before
    fun setup() {
        loginViewModel = LoginViewModel(
            userServices, authentication
        )
        registerViewModel = RegisterViewModel(
            userServices
        )
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            val graph =
                navController.createGraph(startDestination = PokerDiceScreen.Register.name) {
                    composable(PokerDiceScreen.Register.name) { }
                    composable(PokerDiceScreen.Login.name) { }
                    composable(PokerDiceScreen.Title.name) { }
                }
            navController.graph = graph
            RegisterScreen(
                registerViewModel = registerViewModel,
                loginViewModel = loginViewModel,
                onNavigateBack = { navController.navigate(PokerDiceScreen.Login.name) },
                onNavigateToTitle = { navController.navigate(PokerDiceScreen.Title.name) }
            )
        }
    }

    @Test
    fun confirm_RegisterScreen_exists() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(REGISTER_TITLE_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG).assertExists()
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).assertExists()
        composeTestRule.onNodeWithTag(PASSWORD_REQUIREMENTS_TAG).assertExists()
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TEXT_TAG, useUnmergedTree = true).assertExists()
    }

    @Test
    fun successful_Register_Navigates_To_TitleScreen() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("TestUser")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG)
            .performTextInput("PasswordSeguraSoqnao123@gmaildotcom")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsEnabled()
            .performClick()
        composeTestRule.waitForIdle()
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(PokerDiceScreen.Title.name, route)
    }

    @Test
    fun backButton_Navigates_To_LoginScreen() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists().performClick()
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(PokerDiceScreen.Login.name, route)
    }

    @Test
    fun registerButton_Disabled_Due_To_No_Name() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG)
            .performTextInput("PasswordSeguraSoqnao123@gmaildotcom")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_Name_With_Space() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Jose Mourinho")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG)
            .performTextInput("PasswordSeguraSoqnao123@gmaildotcom")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_No_Email() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Test User")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG)
            .performTextInput("PasswordSeguraSoqnao123@gmaildotcom")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_Invalid_Email() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Test User")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG)
            .performTextInput("testuseristonaoeumemail.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG)
            .performTextInput("PasswordSeguraSoqnao123@gmaildotcom")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_No_Invite_Code() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Test User")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG).performTextInput("PASSbug@3223")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_No_Password() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Test User")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG).performTextInput("")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_Short_Password() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Test User")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG).performTextInput("petit")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_Password_Without_Uppercase() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Test User")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG)
            .performTextInput("passsemuppcase@123")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_Password_Without_Lowercase() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Test User")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG)
            .performTextInput("PASSSEMLOWERCASE@123")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_Password_Without_Numbers() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Test User")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG).performTextInput("Passsemnumber@")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }

    @Test
    fun registerButton_Disabled_Due_To_Password_Without_Special_Characters() {
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertExists()
        composeTestRule.onNodeWithTag(CREATE_NAME_INPUT_TAG).performTextInput("Test User")
        composeTestRule.onNodeWithTag(CREATE_EMAIL_INPUT_TAG).performTextInput("testuser@email.com")
        composeTestRule.onNodeWithTag(INVITATION_CODE_INPUT_TAG).performTextInput("HABINTE")
        composeTestRule.onNodeWithTag(CREATE_PASSWORD_INPUT_TAG)
            .performTextInput("PassSemSpecialCharacters123")
        composeTestRule.onNodeWithTag(REGISTER_BUTTON_TAG).assertExists().assertIsNotEnabled()
    }
}