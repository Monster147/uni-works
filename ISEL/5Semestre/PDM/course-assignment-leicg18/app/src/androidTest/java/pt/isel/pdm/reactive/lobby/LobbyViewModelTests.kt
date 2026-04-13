package pt.isel.pdm.reactive.lobby

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Test
import pt.isel.pdm.pokerDice.domain.LobbyCreation
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.Either
import pt.isel.pdm.pokerDice.services.LobbyServices
import pt.isel.pdm.pokerDice.services.UserServices
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.FakeAuthInfoRepo
import pt.isel.pdm.reactive.SuspendingLatch
import java.time.Clock

class LobbyViewModelTests {

    private val trxManager = TransactionManagerInMem()
    private val passwordEncoder = SimpleSha256PasswordEnconder()
    private val tokenEncoder = SimpleTokenEncoder()
    private val userConfig = UsersDomainConfig()
    private val clock = Clock.systemUTC()

    private lateinit var userServices: UserServices
    private lateinit var lobbyServices: LobbyServices

    @Before
    fun setup() {
        userServices = UserServices(
            passwordEncoder,
            tokenEncoder,
            userConfig,
            trxManager,
            clock
        )
        lobbyServices = LobbyServices(trxManager)
    }

    @Test
    fun initially_lobbies_are_empty() = runBlocking {
        val sut = LobbyViewModel(
            lobbyServices,
            userServices,
            FakeAuthInfoRepo()
        )

        assert(sut.lobbiesList.isEmpty()) {
            "Expected lobbiesList to be empty initially"
        }
        assert(sut.allLobbies.value.isEmpty()) {
            "Expected allLobbies to be empty initially"
        }
        assert(sut.availableLobbies.value.isEmpty()) {
            "Expected availableLobbies to be empty initially"
        }
    }

    @Test
    fun fetchLobbies_populates_all_lobbies_and_lobbiesList() = runBlocking {
        val auth = FakeAuthInfoRepo()
        val lobbyCreationVM =
            LobbyCreationViewModel(lobbyServices, userServices, auth)

        lobbyCreationVM.createLobby(
            LobbyCreation("teste", "desc", 2, 2, 10)
        )

        val sut = LobbyViewModel(
            lobbyServices,
            userServices,
            auth
        )

        val latch = SuspendingLatch()

        launch {
            sut.fetchLobbies()
            while (sut.allLobbies.value.isEmpty()) {
                yield()
            }
            latch.open()
        }

        latch.await()

        assert(sut.lobbiesList.size == 5) {
            "Expected lobbiesList to contain five lobby"
        }
        assert(sut.allLobbies.value.size == 5) {
            "Expected allLobbies to contain five lobby"
        }
        assert(sut.allLobbies.value.first().name == "First Lobby")
    }

    @Test
    fun fetchAvailableLobbies_populates_available_lobbies() = runBlocking {
        val auth = FakeAuthInfoRepo()
        val lobbyCreationVM =
            LobbyCreationViewModel(lobbyServices, userServices, auth)

        lobbyCreationVM.createLobby(
            LobbyCreation("teste", "desc", 2, 2, 10)
        )

        val sut = LobbyViewModel(
            lobbyServices,
            userServices,
            auth
        )

        val latch = SuspendingLatch()

        launch {
            sut.fetchAvailableLobbies()
            while (sut.availableLobbies.value.isEmpty()) {
                yield()
            }
            latch.open()
        }

        latch.await()

        // é 5 porque existem 4 lobbies predefinidos no repositório em memória
        assert(sut.availableLobbies.value.size == 5) {
            "Expected availableLobbies to contain five lobby"
        }
    }

    @Test
    fun joinLobby_adds_player_to_lobby() = runBlocking {
        val auth = FakeAuthInfoRepo()
        val lobbyCreationVM =
            LobbyCreationViewModel(lobbyServices, userServices, auth)

        lobbyCreationVM.createLobby(
            LobbyCreation("teste", "desc", 3, 2, 10)
        )

        val lobby = lobbyServices
            .getAllLobbies()
            .first()
            .first()

        val sut = LobbyViewModel(
            lobbyServices,
            userServices,
            auth
        )

        val latch = SuspendingLatch()

        launch {
            sut.joinLobby(lobby)
            val updated =
                lobbyServices.getLobbyById(lobby.id)
            while (
                updated is Either.Right &&
                updated.value.players.size < 2
            ) {
                yield()
            }
            latch.open()
        }

        latch.await()

        val updatedLobby =
            lobbyServices.getLobbyById(lobby.id)

        assert(updatedLobby is Either.Right) {
            "Expected lobby join to succeed"
        }
        assert(updatedLobby is Either.Right && updatedLobby.value.players.size == 2) {
            "Expected lobby to have two players after join"
        }
    }
}