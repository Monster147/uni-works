package pt.isel.daw.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import pt.isel.daw.HandCategory
import pt.isel.daw.Lobby
import pt.isel.daw.LobbyState
import pt.isel.daw.PasswordValidationInfo
import pt.isel.daw.TurnState
import pt.isel.daw.User
import pt.isel.daw.repo.TransactionManager
import kotlin.test.Test

@SpringJUnitConfig(TestConfig::class)
class TestMatchServices {
    @Autowired
    private lateinit var service: MatchServices

    @Autowired
    private lateinit var trxManager: TransactionManager

    private lateinit var host: User
    private lateinit var player2: User
    private lateinit var lobby: Lobby

    @BeforeEach
    fun setup() {
        trxManager.run {
            repoMatches.clear()
            repoLobbies.clear()
            repoUsers.clear()

            host = User(1, "host", "host@email.com", PasswordValidationInfo("hostPWhash"), balance = 100)
            repoUsers.save(host)

            player2 = User(2, "ptwo", "p2@email.com", PasswordValidationInfo("p2PWhash"), balance = 100)
            repoUsers.save(player2)

            lobby =
                Lobby(
                    id = 1,
                    name = "Game",
                    description = "Test Game Lobby",
                    maxPlayers = 2,
                    host = host,
                    players = mutableListOf(host, player2),
                    state = LobbyState.OPEN,
                    rounds = 3,
                    ante = 10,
                )
            repoLobbies.save(lobby)
        }
    }

    @Test
    fun `startMatch by host should create match and update lobby`() {
        val match =
            service.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        assertEquals(lobby.id, match.lobbyId)
        assertEquals(LobbyState.MATCH_IN_PROGRESS, trxManager.run { repoLobbies.getById(lobby.id)?.state })
    }

    @Test
    fun `startMatch fails if not host`() {
        val result =
            service.startMatch(lobby.id, player2)
                .let {
                    check(it is Failure)
                    it.value
                }
        assertTrue(result is MatchError.NotHostOfLobby)
    }

    @Test
    fun `startNextRound advances currentRound`() {
        val match =
            service.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        val result =
            service.startNextRound(match.id)
                .let {
                    check(it is Success)
                    it.value
                }
        assertEquals(1, result.currentRound)
    }

    @Test
    fun `player plays full turn rerolling until max rolls reached`() {
        val match =
            service.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        service.startNextRound(match.id)

        // turn inicial
        var currentMatch =
            service.playTurn(match.id, host, null)
                .let {
                    check(it is Success)
                    it.value
                }

        repeat(2) { // máximo 3 lançamentos (1 inicial + 2 rerolls)
            val turn = currentMatch.rounds.first().turns.first { it.player.id == host.id }
            if (turn.state == TurnState.COMPLETED) return@repeat
            currentMatch =
                service.playTurn(match.id, host, listOf(true, false, true, false, true))
                    .let {
                        check(it is Success)
                        it.value
                    }
        }

        val finalTurn = currentMatch.rounds.first().turns.first { it.player.id == host.id }
        assertEquals(TurnState.COMPLETED, finalTurn.state)
        assertNotNull(finalTurn.score)
        assertTrue(finalTurn.rollCount <= 3)
    }

    @Test
    fun `playTurn rerolls dice when keptDice given`() {
        val match =
            service.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        service.startNextRound(match.id).let {
            check(it is Success)
        }
        val updated =
            service.playTurn(match.id, host, listOf(true, false, true, false, true))
                .let {
                    check(it is Success)
                    it.value
                }
        val turn = updated.rounds.first().turns.first { it.player.id == host.id }
        assertNotNull(turn.score)
        assertTrue(turn.rollCount == 1)
    }

    @Test
    fun `passTurn should complete player turn`() {
        val match =
            service.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        service.startNextRound(match.id)
        val updated =
            service.passTurn(match.id, host)
                .let {
                    check(it is Success)
                    it.value
                }
        val turn = updated.rounds.first().turns.first { it.player.id == host.id }
        assertEquals(TurnState.COMPLETED, turn.state)
    }

    @Test
    fun `player rerolls and then passes turn manually`() {
        val match =
            service.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        service.startNextRound(match.id)

        service.playTurn(match.id, host, null).let {
            check(it is Success)
            it.value
        }

        service.playTurn(match.id, host, listOf(true, false, true, false, true)).let {
            check(it is Success)
            it.value
        }

        val updated =
            service.passTurn(match.id, host).let {
                check(it is Success)
                it.value
            }

        val turn = updated.rounds.first().turns.first { it.player.id == host.id }
        assertEquals(TurnState.COMPLETED, turn.state)
        assertNotNull(turn.score)
    }

    @Test
    fun `finishRound distributes pot and updates balances`() {
        val match =
            service.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        service.startNextRound(match.id)

        trxManager.run {
            val m = repoMatches.getById(match.id) ?: error("Match not found")
            val round = m.rounds.first()
            val updatedTurns = round.turns.map { it.copy(state = TurnState.COMPLETED, score = HandCategory.ONE_PAIR) }
            repoMatches.save(m.copy(rounds = mutableListOf(round.copy(turns = updatedTurns.toMutableList()))))
        }

        val updated =
            service.finishRound(match.id, host)
                .let {
                    check(it is Success)
                    it.value
                }

        val totalBalance = updated.players.sumOf { it.balance }
        assertEquals(200, totalBalance)
    }

    @Test
    fun `finishRound fails if not host`() {
        val match =
            service.startMatch(lobby.id, host).let {
                check(it is Success)
                it.value
            }
        service.startNextRound(match.id)
        val result =
            service.finishRound(match.id, player2)
                .let {
                    check(it is Failure)
                    it.value
                }
        assertTrue(result is MatchError.NotHostOfLobby)
    }
}
