package pt.isel.pdm.reactive.lobby

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.LobbyCreation
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.LobbyError
import pt.isel.pdm.pokerDice.services.LobbyServiceInterface
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.dto.LobbyEvent
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationState
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import pt.isel.pdm.reactive.SuspendingLatch
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class LobbyCreationViewModelTests {

    private val trxManager = TransactionManagerInMem()
    private val passwordEncoder = SimpleSha256PasswordEnconder()
    private val tokenEncoder = SimpleTokenEncoder()
    private val userConfig = UsersDomainConfig()
    private val clock = Clock.systemUTC()
    private lateinit var userServices: UserServices
    private lateinit var lobbyServices: LobbyServices

    @Before
    fun setup() {
        userServices = UserServices(passwordEncoder, tokenEncoder, userConfig, trxManager, clock)
        lobbyServices = LobbyServices(trxManager)
    }

    @Test
    fun `initial state is Idle`() = runBlocking {
        val sut = LobbyCreationViewModel(lobbyServices, userServices, FakeAuthInfoRepo())
        assert(sut.lobbyCreationState is LobbyCreationState.Idle) {
            "Expected initial state to be Idle, but got ${sut.lobbyCreationState}"
        }
    }

    @Test
    fun `createLobby changes state to Loading`() = runBlocking {
        val sut = LobbyCreationViewModel(lobbyServices, userServices, FakeAuthInfoRepo())
        val lobbyDetails = LobbyCreation("teste", "desc", 2, 2, 10)
        val latch = SuspendingLatch()

        launch {
            sut.createLobby(lobbyDetails)
            while (sut.lobbyCreationState is LobbyCreationState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.lobbyCreationState !is LobbyCreationState.Idle) {
            "Expected state to change from Idle after createLobby, but got ${sut.lobbyCreationState}"
        }
    }

    @Test
    fun `createLobby changes state to Success when creation succeeds`() = runBlocking {
        val sut = LobbyCreationViewModel(lobbyServices, userServices, FakeAuthInfoRepo())
        val lobbyDetails = LobbyCreation("teste", "desc", 2, 2, 10)
        val latch = SuspendingLatch()

        launch {
            sut.createLobby(lobbyDetails)
            while (sut.lobbyCreationState is LobbyCreationState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }

        latch.await()
        assert(sut.lobbyCreationState is LobbyCreationState.Success) {
            "Expected state to be Success after creating lobby, but got ${sut.lobbyCreationState}"
        }
    }

    @Test
    fun `createLobby changes state to Error when name already taken`() = runBlocking {
        val sut = LobbyCreationViewModel(lobbyServices, userServices, FakeAuthInfoRepo())
        val lobby1 = LobbyCreation("teste", "desc", 2, 2, 10)
        val lobby2 = LobbyCreation("teste", "desc2", 2, 2, 10)
        val latch = SuspendingLatch()

        launch {
            sut.createLobby(lobby1)
            while (sut.lobbyCreationState is LobbyCreationState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }
        latch.await()
        sut.resetLobbyCreationState()

        val latch2 = SuspendingLatch()
        launch {
            sut.createLobby(lobby2)
            while (sut.lobbyCreationState is LobbyCreationState.Loading) kotlinx.coroutines.yield()
            latch2.open()
        }
        latch2.await()

        assert(sut.lobbyCreationState is LobbyCreationState.Error) {
            "Expected state to be Error for duplicate lobby name, but got ${sut.lobbyCreationState}"
        }

        val errMsg = (sut.lobbyCreationState as LobbyCreationState.Error).message
        assert(errMsg == "Failed to create lobby: Lobby name already in use!") {
            "Expected error message to be 'Failed to create lobby: Lobby name already in use!' but was '$errMsg'"
        }
    }

    @Test
    fun `createLobby changes state to Error when unexpected failure occurs`() = runBlocking {
        val fakeService = FakeLobbyServiceUnexpected()
        val sut = LobbyCreationViewModel(fakeService, userServices, FakeAuthInfoRepo())
        val lobbyDetails = LobbyCreation("teste", "unexpected", 2, 2, 10)
        val latch = SuspendingLatch()

        launch {
            sut.createLobby(lobbyDetails)
            while (sut.lobbyCreationState is LobbyCreationState.Loading) kotlinx.coroutines.yield()
            latch.open()
        }
        latch.await()

        assert(sut.lobbyCreationState is LobbyCreationState.Error) {
            "Expected state to be Error for unexpected failure, but got ${sut.lobbyCreationState}"
        }

        val errMsg = (sut.lobbyCreationState as LobbyCreationState.Error).message
        assert(errMsg == "Failed to create lobby: Unexpected Error") {
            "Expected error message to be 'Failed to create lobby: Unexpected Error' but was '$errMsg'"
        }
    }
}

class FakeLobbyServiceUnexpected : LobbyServiceInterface {
    override suspend fun createLobby(
        name: String,
        description: String,
        maxPlayers: Int,
        host: User,
        rounds: Int,
        ante: Int
    ) = Either.Left(LobbyError.NotHost) // any error other than name taken

    override suspend fun joinLobby(lobbyID: Int, player: User) = Either.Left(LobbyError.NotFound)
    override suspend fun leaveLobby(lobbyID: Int, player: User) = Either.Left(LobbyError.NotFound)
    override fun getAllLobbies() = emptyFlow<List<Lobby>>()
    override fun getAllAvailableLobbies() = emptyFlow<List<Lobby>>()
    override suspend fun getLobbyById(lobbyID: Int) = Either.Left(LobbyError.NotFound)
    override suspend fun deleteLobby(lobbyID: Int, requester: User) = Either.Left(LobbyError.NotFound)
    override fun subscribeToLobbyEvents(lobbyID: Int): Flow<LobbyEvent> {
        TODO("Not yet implemented")
    }
}
