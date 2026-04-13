package pt.isel.pdm.reactive.Login

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.UserCredentials
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginState
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import pt.isel.pdm.reactive.SuspendingLatch
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTests {

    private val trxManager = TransactionManagerInMem()
    private val passwordEncoder = SimpleSha256PasswordEnconder()
    private val tokenEncoder = SimpleTokenEncoder()
    private val userConfig = UsersDomainConfig()
    private val clock = Clock.systemUTC()
    private lateinit var userServices: UserServices
    private lateinit var authRepo: FakeAuthInfoRepo

    @Before
    fun setup() {
        userServices = UserServices(passwordEncoder, tokenEncoder, userConfig, trxManager, clock)
        authRepo = FakeAuthInfoRepo()
    }

    @Test
    fun `initial state is Idle`() = runBlocking {
        val sut = LoginViewModel(userServices, authRepo)
        assert(sut.loginState is LoginState.Idle) {
            "Expected initial state to be Idle, but got ${sut.loginState}"
        }
    }

    @Test
    fun `login changes state to Loading`() = runBlocking {
        val sut = LoginViewModel(userServices, authRepo)
        val credentials = UserCredentials("teste@gmail.com", "Teste@123")

        val latch = SuspendingLatch()
        launch {
            sut.login(credentials)
            while (sut.loginState is LoginState.Loading) yield()
            latch.open()
        }

        latch.await()
        assert(sut.loginState !is LoginState.Idle) {
            "Expected state to not be Idle after login, but got ${sut.loginState}"
        }
    }

    @Test
    fun `successful login changes state to Success`() = runBlocking {
        val email = "teste@gmail.com"
        val password = "Teste@123"
        val createResult = userServices.createUser("Test", email, password, "skip")
        assert(createResult is Either.Right) { "Failed to create test user: $createResult" }

        val sut = LoginViewModel(userServices, authRepo)
        val latch = SuspendingLatch()

        launch {
            sut.login(UserCredentials(email, password))
            while (sut.loginState is LoginState.Loading) yield()
            latch.open()
        }

        latch.await()

        assert(sut.loginState is LoginState.Success) {
            "Expected state to be Success after login, but got ${sut.loginState}"
        }

        val savedAuth = authRepo.authInfo.first()
        assert(savedAuth?.userEmail == email) {
            "Expected saved auth email to be $email but was ${savedAuth?.userEmail}"
        }
    }

    @Test
    fun `failed login changes state to Error`() = runBlocking {
        val sut = LoginViewModel(userServices, authRepo)
        val credentials = UserCredentials("nonexistent@user.com", "wrongPassword@321")
        val latch = SuspendingLatch()

        launch {
            sut.login(credentials)
            while (sut.loginState is LoginState.Loading) yield()
            latch.open()
        }

        latch.await()

        assert(sut.loginState is LoginState.Error) {
            "Expected state to be Error after failed login, but got ${sut.loginState}"
        }

        val errMsg = (sut.loginState as LoginState.Error).message
        assert(errMsg == "Invalid email or password") {
            "Expected error message to be 'Invalid email or password', but was '$errMsg'"
        }
    }

    @Test
    fun `login does nothing if already Loading or Success`() = runBlocking {
        val email = "teste@gmail.com"
        val password = "Teste@123"
        userServices.createUser("Test", email, password, "skip")

        val sut = LoginViewModel(userServices, authRepo)
        val latch = SuspendingLatch()

        launch {
            sut.login(UserCredentials(email, password))
            while (sut.loginState is LoginState.Loading) yield()
            latch.open()
        }

        latch.await()
        val previousState = sut.loginState

        sut.login(UserCredentials(email, password))

        assert(sut.loginState == previousState) {
            "Expected state to remain the same when login is called during Loading/Success, but changed from $previousState to ${sut.loginState}"
        }
    }
}