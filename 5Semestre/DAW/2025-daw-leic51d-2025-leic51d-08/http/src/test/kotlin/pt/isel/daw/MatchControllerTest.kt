package pt.isel.daw

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.isel.daw.http.MatchController
import pt.isel.daw.http.model.PlayTurnInputModel
import pt.isel.daw.repo.TransactionManager
import pt.isel.daw.service.MatchServices
import pt.isel.daw.service.Success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringJUnitConfig(TestConfig::class)
class MatchControllerTest {
    @Autowired
    private lateinit var trxManager: TransactionManager

    @Autowired
    private lateinit var matchServices: MatchServices

    @Autowired
    private lateinit var matchController: MatchController

    private lateinit var host: User
    private lateinit var player2: User
    private lateinit var lobby: Lobby

    private lateinit var authHost: AuthenticatedUser
    private lateinit var authPlayer2: AuthenticatedUser

    @BeforeEach
    fun setup() {
        trxManager.run {
            repoMatches.clear()
            repoLobbies.clear()
            repoUsers.clear()

            host = User(1, "host", "host@mail.com", PasswordValidationInfo("hostPWhash"), balance = 100)
            repoUsers.save(host)

            player2 = User(2, "player2", "p2@mail.com", PasswordValidationInfo("p2PWhash"), balance = 100)
            repoUsers.save(player2)

            lobby =
                Lobby(
                    id = 1,
                    name = "Test Lobby",
                    description = "Testing",
                    maxPlayers = 2,
                    host = host,
                    players = mutableListOf(host, player2),
                    state = LobbyState.OPEN,
                    rounds = 3,
                    ante = 10,
                )
            repoLobbies.save(lobby)
        }

        authHost = AuthenticatedUser(host, "token-host")
        authPlayer2 = AuthenticatedUser(player2, "token-p2")
    }

    @Test
    fun `start match successfully by host`() {
        val response = matchController.startMatch(authHost, lobby.id)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.getFirst(HttpHeaders.LOCATION)
        assertNotNull(location)
        assertTrue(location.startsWith("/api/matches/"))
    }

    @Test
    fun `start match fails if not host`() {
        val response = matchController.startMatch(authPlayer2, lobby.id)
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `start match fails if lobby does not exist`() {
        val response = matchController.startMatch(authHost, 9999)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `start next round successfully`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        val response = matchController.startNextRound(match.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        val location = response.headers.getFirst(HttpHeaders.LOCATION)
        assertNotNull(location)
        assertTrue(location.startsWith("/api/matches/${match.id}/rounds/"))
    }

    @Test
    fun `start next round fails if match does not exist`() {
        val response = matchController.startNextRound(9999)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `start next round fails if no rounds defined`() {
        val emptyLobby =
            Lobby(
                id = 2,
                name = "Empty Lobby",
                description = "No rounds",
                maxPlayers = 2,
                host = host,
                players = mutableListOf(host, player2),
                state = LobbyState.OPEN,
                rounds = 0,
                ante = 10,
            )
        trxManager.run { repoLobbies.save(emptyLobby) }
        val matchResult = matchServices.startMatch(emptyLobby.id, host)
        check(matchResult is Success)
        val response = matchController.startNextRound(matchResult.value.id)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `play turn successfully`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        matchServices.startNextRound(match.id)

        val playInput = PlayTurnInputModel(keptDice = listOf(true, false, true, false, true))
        val response = matchController.playTurn(match.id, authHost, playInput)
        assertEquals(HttpStatus.OK, response.statusCode)
        val location = response.headers.getFirst(HttpHeaders.LOCATION)
        assertNotNull(location)
        assertTrue(location.contains("/rounds/"))
    }

    @Test
    fun `play turn fails if match not in progress`() {
        val matchResult = matchServices.startMatch(lobby.id, host)
        check(matchResult is Success)
        val matchId = matchResult.value.id

        trxManager.run {
            val m = checkNotNull(repoMatches.getById(matchId))
            repoMatches.save(m.copy(state = MatchState.COMPLETED))
        }

        val playInput = PlayTurnInputModel(listOf(true, true, true, true, true))
        val response = matchController.playTurn(matchId, authHost, playInput)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `play turn fails if not your turn`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        matchServices.startNextRound(match.id)

        val playInput = PlayTurnInputModel()
        val response = matchController.playTurn(match.id, authPlayer2, playInput)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `play turn fails if keptDice invalid length`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        matchServices.startNextRound(match.id)

        val invalidInput = PlayTurnInputModel(listOf(true, false))
        val response = matchController.playTurn(match.id, authHost, invalidInput)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `pass turn successfully`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        matchServices.startNextRound(match.id)

        val response = matchController.passTurn(match.id, authHost)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `pass turn fails if player already played`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        matchServices.startNextRound(match.id)

        trxManager.run {
            val m = checkNotNull(repoMatches.getById(match.id))
            val round = m.rounds.first()
            val updatedTurns = listOf(round.turns.first().copy(state = TurnState.COMPLETED))
            repoMatches.save(m.copy(rounds = mutableListOf(round.copy(turns = updatedTurns.toMutableList()))))
        }

        val response = matchController.passTurn(match.id, authHost)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `finish round successfully`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        matchServices.startNextRound(match.id)

        // Marca todos os turnos como COMPLETED para permitir terminar a ronda
        trxManager.run {
            val m = repoMatches.getById(match.id)!!
            val round = m.rounds.first()
            val updatedTurns = round.turns.map { it.copy(state = TurnState.COMPLETED) }
            repoMatches.save(m.copy(rounds = mutableListOf(round.copy(turns = updatedTurns.toMutableList()))))
        }

        val response = matchController.finishRound(match.id, authHost)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `finish round fails if not host`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        matchServices.startNextRound(match.id)

        val response = matchController.finishRound(match.id, authPlayer2)
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `finish round fails if match does not exist`() {
        val response = matchController.finishRound(9999, authHost)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `finish round fails if not all turns completed`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        matchServices.startNextRound(match.id)

        val response = matchController.finishRound(match.id, authHost)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `get match returns match`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        val response = matchController.getMatch(match.id)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `get match returns not found`() {
        val response = matchController.getMatch(9999)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `get round returns round`() {
        val match =
            matchServices.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        matchServices.startNextRound(match.id)
        val response = matchController.getRound(match.id, 1)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `get round returns not found`() {
        val response = matchController.getRound(9999, 1)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
