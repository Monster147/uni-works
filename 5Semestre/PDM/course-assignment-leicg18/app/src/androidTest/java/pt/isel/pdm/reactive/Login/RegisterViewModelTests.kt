package pt.isel.pdm.reactive.Login

import androidx.test.core.app.ActivityScenario.launch
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.UserRegisterCredentials
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.Failure
import pt.isel.pdm.pokerDice.services.UserError
import pt.isel.pdm.pokerDice.services.UserServiceInterface
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.login_register.RegisterState
import pt.isel.pdm.pokerDice.viewModels.login_register.RegisterViewModel
import pt.isel.pdm.reactive.SuspendingLatch
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTests {

    private val trxManager = TransactionManagerInMem()
    private val passwordEncoder = SimpleSha256PasswordEnconder()
    private val tokenEncoder = SimpleTokenEncoder()
    private val userConfig = UsersDomainConfig()
    private val clock = Clock.systemUTC()
    private lateinit var userServices: UserServices

    @Before
    fun setup() {
        userServices = UserServices(passwordEncoder, tokenEncoder, userConfig, trxManager, clock)
    }

    @Test
    fun `initial state is Idle`() = runBlocking {
        val sut = RegisterViewModel(userServices)
        assert(sut.registerState is RegisterState.Idle) {
            "Expected initial state to be Idle, but got ${sut.registerState}"
        }
    }

    @Test
    fun `addUser changes state to Loading`() = runBlocking {
        val sut = RegisterViewModel(userServices)
        val userCredentials = UserRegisterCredentials("RuiTosta", "teste@gmail.com", "Teste@123", "skip")
        val latch = SuspendingLatch()

        launch {
            sut.addUser(userCredentials)
            while (sut.registerState is RegisterState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.registerState !is RegisterState.Idle) {
            "Expected state to change from Idle after addUser, but got ${sut.registerState}"
        }
    }

    @Test
    fun `addUser changes state to Success when registration succeeds`() = runBlocking {
        val sut = RegisterViewModel(userServices)
        val userCredentials = UserRegisterCredentials("RuiTosta", "teste@gmail.com", "Teste@123", "skip")
        val latch = SuspendingLatch()

        launch {
            sut.addUser(userCredentials)
            while (sut.registerState is RegisterState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.registerState is RegisterState.Success) {
            "Expected state to be Success after successful registration, but got ${sut.registerState}"
        }

        val creds = (sut.registerState as RegisterState.Success).credentials
        assert(creds.email == userCredentials.email && creds.password == userCredentials.password) {
            "Expected credentials to match input, but got $creds"
        }
    }

    @Test
    fun `addUser changes state to Error when email is duplicate`() = runBlocking {
        val email = "duplicate@test.com"
        val password = "existentUser@321"
        val name = "RuiEncosta"

        userServices.createUser(name, email, password, "skip")

        val sut = RegisterViewModel(userServices)
        val userCredentials = UserRegisterCredentials(name, email, password, "skip")
        val latch = SuspendingLatch()

        launch {
            sut.addUser(userCredentials)
            while (sut.registerState is RegisterState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.registerState is RegisterState.Error) {
            "Expected state to be Error after duplicate email registration, but got ${sut.registerState}"
        }

        val errMsg = (sut.registerState as RegisterState.Error).message
        assert(errMsg == "Email address is already in use") {
            "Expected error message to be 'Email address is already in use' but was '$errMsg'"
        }
    }

    @Test
    fun `addUser changes state to Error when password is insecure`() = runBlocking {
        val fakeSvc = FakeUserServiceInsecurePassword(userServices)
        val sut = RegisterViewModel(fakeSvc)
        val userCredentials = UserRegisterCredentials("RuiEncosta", "unique@test.com", "passvC@123", "skip")
        val latch = SuspendingLatch()

        launch {
            sut.addUser(userCredentials)
            while (sut.registerState is RegisterState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.registerState is RegisterState.Error) {
            "Expected state to be Error after insecure password, but got ${sut.registerState}"
        }

        val errMsg = (sut.registerState as RegisterState.Error).message
        assert(errMsg == "The password is not secure enough") {
            "Expected error message to be 'The password is not secure enough', but was '$errMsg'"
        }
    }
}

class FakeUserServiceInsecurePassword(private val delegate: UserServices) :
    UserServiceInterface by delegate {
    override suspend fun createUser(
        name: String,
        email: String,
        password: String,
        inviteCode: String
    ) = Failure(UserError.InsecurePassword)
}