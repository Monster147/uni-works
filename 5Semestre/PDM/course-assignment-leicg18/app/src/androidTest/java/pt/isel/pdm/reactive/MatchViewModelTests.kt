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
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.LobbyCreation
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.Round
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.MatchServices
import pt.isel.pdm.pokerDice.services.Success
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.dto.MatchEvent
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.HostState
import pt.isel.pdm.pokerDice.viewModels.MatchViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfo
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTests {

    private val trxManager = TransactionManagerInMem()
    private val passwordEncoder = SimpleSha256PasswordEnconder()
    private val tokenEncoder = SimpleTokenEncoder()
    private val userConfig = UsersDomainConfig()
    private val clock = Clock.systemUTC()

    private lateinit var userServices: UserServices
    private lateinit var lobbyServices: LobbyServices
    private lateinit var matchServices: MatchServices
    private lateinit var authRepo: FakeAuthInfoRepo

    @Before
    fun setup() {
        userServices =
            UserServices(passwordEncoder, tokenEncoder, userConfig, trxManager, clock)
        lobbyServices = LobbyServices(trxManager)
        matchServices = MatchServices(trxManager)
        authRepo = FakeAuthInfoRepo()
    }

    @Test
    fun `initial state has no match and hostState is Loading`() = runBlocking {
        val sut =
            MatchViewModel(
                matchServices,
                userServices,
                authRepo,
                lobbyServices
            )

        assert(sut.currentMatch.value == null)
        assert(sut.hostState.value is HostState.Loading)
    }

    @Test
    fun `fetchMatch sets HostState to Host when logged user is lobby host`() = runBlocking {
        val email = "host@test.com"
        val password = "Host@123"

        val user = userServices.createUser("Host", email, password, "skip").let{
            check(it is Success)
            it
        }
        val token = userServices.createToken(email, password).let{
            check(it is Success)
            it
        }

        authRepo.saveAuthInfo(
            AuthInfo(
                userEmail = email,
                authToken = token.value.tokenValue,
                password = password
            )
        )

        val lobby = lobbyServices.createLobby(
            "TesteLobbyDetailsViewModel",
            "Teste",
            2,
            user.value,
            2,
            10
        ).let{
            check(it is Success)
            it
        }

        val match =
            (matchServices.startMatch(lobby.value.id, user.value) as Either.Right).value

        val sut =
            MatchViewModel(
                matchServices,
                userServices,
                authRepo,
                lobbyServices
            )

        val latch = SuspendingLatch()

        launch {
            sut.fetchMatch(match.id)
            while (sut.currentMatch.value == null) yield()
            while (sut.hostState.value is HostState.Loading) yield()
            latch.open()
        }

        latch.await()

        assert(sut.hostState.value is HostState.Host) {
            "Expected HostState.Host, but got ${sut.hostState.value}"
        }
    }

    @Test
    fun `fetchMatch sets HostState to NotHost when user is not lobby host`() = runBlocking {
        val email = "host@test.com"
        val password = "Host@123"

        val user = userServices.createUser("Host", email, password, "skip").let{
            check(it is Success)
            it
        }

        val token = userServices.createToken(email, password).let{
            check(it is Success)
            it
        }

        authRepo.saveAuthInfo(
            AuthInfo(
                userEmail = email,
                authToken = token.value.tokenValue,
                password = password
            )
        )

        val lobby = lobbyServices.createLobby(
            "TesteLobbyDetailsViewModel",
            "Teste",
            2,
            user.value,
            2,
            10
        ).let{
            check(it is Success)
            it
        }

        val match =
            (matchServices.startMatch(lobby.value.id, user.value) as Either.Right).value

        val otherUser = userServices.createUser("Other", "other@test.com", "Other@123", "skip").let{
            check(it is Success)
            it
        }

        val otherToken = userServices.createToken("other@test.com", "Other@123").let{
            check(it is Success)
            it
        }


        authRepo.saveAuthInfo(
            AuthInfo(
                userEmail = "other@test.com",
                authToken = otherToken.value.tokenValue,
                password = "Other@123"
            )
        )

        val sut =
            MatchViewModel(
                matchServices,
                userServices,
                authRepo,
                lobbyServices
            )

        val latch = SuspendingLatch()

        launch {
            sut.fetchMatch(match.id)
            while (sut.hostState.value is HostState.Loading) yield()
            latch.open()
        }

        latch.await()

        assert(sut.hostState.value is HostState.NotHost) {
            "Expected HostState.NotHost, but got ${sut.hostState.value}"
        }
    }
}