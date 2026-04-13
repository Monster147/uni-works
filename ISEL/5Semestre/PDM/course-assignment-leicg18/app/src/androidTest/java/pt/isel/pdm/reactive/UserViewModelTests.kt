package pt.isel.pdm.reactive

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
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.InvitationServices
import pt.isel.pdm.pokerDice.services.InvitationServicesInterface
import pt.isel.pdm.pokerDice.services.Success
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.ProfileState
import pt.isel.pdm.pokerDice.viewModels.UserViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfo
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTests {

    private lateinit var userServices: UserServices
    private lateinit var invitationServices: InvitationServices

    @Before
    fun setup() {
        val trxManager = TransactionManagerInMem()
        userServices = UserServices(
            SimpleSha256PasswordEnconder(),
            SimpleTokenEncoder(),
            UsersDomainConfig(),
            trxManager,
            Clock.systemUTC()
        )
        invitationServices = InvitationServices(trxManager)
    }

    @Test
    fun `initial state is Idle`() = runBlocking {
        val sut = UserViewModel(userServices, FakeAuthInfoRepo(), invitationServices)
        assert(sut.state is ProfileState.Idle) {
            "Expected initial state to be Idle, but got ${sut.state}"
        }
    }

    @Test
    fun `fetchUser changes state to Loading`() = runBlocking {
        val sut = UserViewModel(userServices, FakeAuthInfoRepo(), invitationServices)
        val latch = SuspendingLatch()

        launch {
            sut.fetchUser()
            while (sut.state is ProfileState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.state !is ProfileState.Idle) {
            "Expected state to change from Idle after fetchUser, but got ${sut.state}"
        }
    }

    @Test
    fun `fetchUser changes state to Success when fetch succeeds`() = runBlocking {
        val email = "teste@gmail.com"
        val password = "Teste@123"

        // Create test user
        val createdUser = userServices.createUser("TestUser", email, password, "skip").let {
            check(it is pt.isel.pdm.pokerDice.services.Either.Right)
            it.value
        }

        // Create token and save in FakeAuthInfoRepo
        val token = userServices.createToken(email, password).let {
            check(it is pt.isel.pdm.pokerDice.services.Either.Right)
            it.value.tokenValue
        }
        val authRepo = FakeAuthInfoRepo()
        authRepo.saveAuthInfo(AuthInfo(email, token, password))

        val sut = UserViewModel(userServices, authRepo, invitationServices)
        val latch = SuspendingLatch()

        launch {
            sut.fetchUser()
            while (sut.state is ProfileState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.state is ProfileState.Success) {
            "Expected state to be Success after fetchUser, but got ${sut.state}"
        }
        assert(sut.user == createdUser) {
            "Expected fetched user to be $createdUser, but got ${sut.user}"
        }
    }

    @Test
    fun `fetchUser changes state to Unauthenticated when fetch fails`() = runBlocking {
        val sut = UserViewModel(userServices, FakeAuthInfoRepo(), invitationServices)
        val latch = SuspendingLatch()

        launch {
            sut.fetchUser()
            while (sut.state is ProfileState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.state is ProfileState.Unauthenticated) {
            "Expected state to be Unauthenticated after failed fetch, but got ${sut.state}"
        }
    }

    @Test
    fun `logout changes state to Unauthenticated`() = runBlocking {
        val authInfo = AuthInfo("teste@gmail.com", "valid_token", "Teste@123")
        val authRepo = FakeAuthInfoRepo()
        authRepo.saveAuthInfo(authInfo)
        val sut = UserViewModel(userServices, authRepo, invitationServices)
        val latch = SuspendingLatch()

        launch {
            sut.logout()
            while (sut.state is ProfileState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.state is ProfileState.Unauthenticated) {
            "Expected state to be Unauthenticated after logout, but got ${sut.state}"
        }
    }
}