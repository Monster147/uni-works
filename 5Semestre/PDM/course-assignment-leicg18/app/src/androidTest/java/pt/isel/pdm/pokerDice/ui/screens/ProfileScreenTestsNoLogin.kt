package pt.isel.pdm.pokerDice.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.main.PokerDiceScreen
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.InvitationServices
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.ProfileState
import pt.isel.pdm.pokerDice.viewModels.UserViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

class ProfileScreenTestsNoLogin {
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
    fun setupAppNavHost() {

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
    fun profileScreen_showsLoginPrompt_whenNoUserLoggedIn() {
        composeTestRule.waitUntil(
            condition = {
                userViewModel.state is ProfileState.Unauthenticated
            },
            timeoutMillis = 5_000
        )

        composeTestRule.waitForIdle()
        assert(navController.currentBackStackEntry?.destination?.route == PokerDiceScreen.Login.name)
    }
}