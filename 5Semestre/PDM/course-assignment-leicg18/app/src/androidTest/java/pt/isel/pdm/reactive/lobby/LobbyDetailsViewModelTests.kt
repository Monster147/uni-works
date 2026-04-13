package pt.isel.pdm.reactive.lobby

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.LobbyServiceInterface
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.MatchServiceInterface
import pt.isel.pdm.pokerDice.services.MatchServices
import pt.isel.pdm.pokerDice.services.Success
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyDetailsViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.MatchState
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfo
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import pt.isel.pdm.reactive.SuspendingLatch
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyDetailsViewModelTests {

    private val trxManager = TransactionManagerInMem()
    private lateinit var lobbyServices: LobbyServiceInterface
    private lateinit var userServices: UserServices
    private lateinit var matchServices: MatchServiceInterface
    private lateinit var authRepo: FakeAuthInfoRepo

    private val passwordEncoder = SimpleSha256PasswordEnconder()
    private val tokenEncoder = SimpleTokenEncoder()
    private val userConfig = UsersDomainConfig()
    private val clock = Clock.systemUTC()

    @Before
    fun setup() {
        userServices =
            UserServices(
                passwordEncoder,
                tokenEncoder,
                userConfig,
                trxManager,
                clock
            )

        lobbyServices = LobbyServices(trxManager)
        matchServices = MatchServices(trxManager)
        authRepo = FakeAuthInfoRepo()
    }

    @Test
    fun `initial state is correct`() = runBlocking {
        val sut =
            LobbyDetailsViewModel(
                lobbyServices,
                userServices,
                matchServices,
                authRepo
            )

        assert(sut.currentLobby.value == null) {
            "Expected currentLobby to be null initially"
        }

        assert(sut.matchState is MatchState.Idle) {
            "Expected matchState to be Idle initially, but was ${sut.matchState}"
        }

        assert(!sut.isHost) {
            "Expected isHost to be false initially"
        }
    }

    @Test
    fun `fetchLobbyById populates currentLobby`() = runBlocking {
        val sut =
            LobbyDetailsViewModel(
                lobbyServices,
                userServices,
                matchServices,
                authRepo
            )

        val latch = SuspendingLatch()

        launch {
            sut.fetchLobbyById(1)
            while (sut.currentLobby.value == null) {
                yield()
            }
            latch.open()
        }

        latch.await()

        val lobby = sut.currentLobby.value
        assert(lobby != null) {
            "Expected currentLobby to be populated"
        }
        assert(lobby!!.id == 1) {
            "Expected lobby id to be 1, but was ${lobby.id}"
        }
    }

    @Test
    fun `checkHost sets isHost to true when user is host`() = runBlocking {
        val hostEmail = "host@test.com"
        val hostPassword = "Host@123"

        val user = userServices.createUser("Host", hostEmail, hostPassword, "skip").let{
            check(it is Success)
            it
        }

        val loginResult =
            userServices.createToken(hostEmail, hostPassword).let{
                check(it is Success)
                it
            }


        authRepo.saveAuthInfo(
            AuthInfo(
                userEmail = hostEmail,
                authToken = loginResult.value.tokenValue,
                password = hostPassword
            )
        )

        val lobbyResult = lobbyServices.createLobby(
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

        val sut =
            LobbyDetailsViewModel(
                lobbyServices,
                userServices,
                matchServices,
                authRepo
            )

        val latch = SuspendingLatch()

        launch {
            sut.fetchLobbyById(lobbyResult.value.id)
            while (sut.currentLobby.value == null) yield()

            sut.checkHost()
            while (!sut.isHost) yield()

            latch.open()
        }

        latch.await()

        assert(sut.isHost) {
            "Expected isHost to be true when logged user is the lobby host"
        }
    }

    @Test
    fun `leaveLobby removes player from lobby`() = runBlocking {
        val lobby = lobbyServices.getLobbyById(1).let{
            check(it is Success)
            it
        }

        val initialPlayers = lobby.value.players.size

        val sut =
            LobbyDetailsViewModel(
                lobbyServices,
                userServices,
                matchServices,
                authRepo
            )

        val latch = SuspendingLatch()

        launch {
            sut.leaveLobby(lobby.value)

            while (true) {
                val updatedLobby = lobbyServices.getLobbyById(1)
                if (updatedLobby is Success &&
                    updatedLobby.value.players.size < initialPlayers
                ) {
                    latch.open()
                    break
                }
                yield()
            }
        }

        latch.await()

        val updatedLobby = lobbyServices.getLobbyById(1) as Success
        assert(updatedLobby.value.players.size == initialPlayers - 1) {
            "Expected one player to be removed from the lobby"
        }
    }

    @Test
    fun `startMatch changes state to Loading then Success`() = runBlocking {
        val lobbyResult = lobbyServices.getLobbyById(1).let{
            check(it is Success)
            it
        }

        val lobby = lobbyResult.value

        val sut =
            LobbyDetailsViewModel(
                lobbyServices,
                userServices,
                matchServices,
                authRepo
            )

        val latch = SuspendingLatch()

        launch {
            sut.startMatch(lobby)

            while (sut.matchState is MatchState.Loading) {
                yield()
            }

            latch.open()
        }

        latch.await()

        assert(sut.matchState is MatchState.Success) {
            "Expected matchState to be Success after starting match, but was ${sut.matchState}"
        }
    }

    @Test
    fun `startMatch fails when not host`() = runBlocking {
        val lobbyResult = lobbyServices.getLobbyById(1).let{
            check(it is Success)
            it
        }

        val lobby = lobbyResult.value.copy(
            host = lobbyResult.value.players.last()
        )

        val sut =
            LobbyDetailsViewModel(
                lobbyServices,
                userServices,
                matchServices,
                authRepo
            )

        val latch = SuspendingLatch()

        launch {
            sut.startMatch(lobby)

            while (sut.matchState is MatchState.Loading) {
                yield()
            }

            latch.open()
        }

        latch.await()

        assert(sut.matchState is MatchState.Error) {
            "Expected matchState to be Error when starting match as non-host"
        }
    }
}