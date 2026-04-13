package pt.isel.pdm.pokerDice.repo.mem

import pt.isel.pdm.pokerDice.domain.Hand
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Round
import pt.isel.pdm.pokerDice.domain.Turn
import pt.isel.pdm.pokerDice.domain.TurnState
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.repo.RepositoryMatches

class RepositoryMatchesInMem : RepositoryMatches {
    private val matches = mutableListOf<Match>(
        /*Match(
            id = 1,
            lobbyId = 1,
            players = mutableListOf(
                User(1000, "Alice", "alice@gmail.com", PasswordValidationInfo("hash")),
                User(1001, "Bob", "bob@gmail.com", PasswordValidationInfo("hash"))
            ),
            rounds = mutableListOf(
                Round(
                    id = 1,
                    matchId = 1,
                    roundNumber = 1,
                    turns = mutableListOf(
                        Turn(
                            id = 1,
                            roundId = 1,
                            player = User(
                                1000,
                                "Alice",
                                "alice@gmail.com",
                                PasswordValidationInfo("hash")
                            ),
                            hand = Hand(),
                            rollCount = 0,
                            state = TurnState.IN_PROGRESS,
                            score = null,
                        ),
                        Turn(
                            id = 2,
                            roundId = 1,
                            player = User(
                                1001,
                                "Bob",
                                "bob@gmail.com",
                                PasswordValidationInfo("hash")
                            ),
                            hand = Hand(),
                            rollCount = 0,
                            state = TurnState.IN_PROGRESS,
                            score = null
                        )
                    ),
                    currentPlayer = User(
                        1000,
                        "Alice",
                        "alice@gmail.com",
                        PasswordValidationInfo("hash")
                    )
                )
            ),
            currentRound = 1,
        ),*/
    )
    private val repoRound = RepositoryRoundsInMem()
    private val repoTurn = RepositoryTurnsInMem()

    override fun createMatch(lobby: Lobby): Match {
        val matchId = matches.size + 1
        return Match(
            id = matchId,
            lobbyId = lobby.id,
            players = lobby.players,
            rounds = createRoundsForMatch(matchId, lobby).toMutableList(),
            currentRound = 0,
        ).also { matches.add(it) }
    }

    override fun createRoundsForMatch(
        matchId: Int,
        lobby: Lobby,
    ): List<Round> {
        return (1..lobby.rounds).mapIndexed { idx, rnum ->
            val round =
                repoRound.createRound(Round(idx, matchId, rnum, mutableListOf(), lobby.players[0]))
            round.turns.addAll(createTurnsForRound(round, lobby.players))
            round
        }
    }

    override fun createTurnsForRound(
        round: Round,
        players: List<User>,
    ): List<Turn> {
        return players.mapIndexed { idx, player ->
            repoTurn.createTurn(
                Turn(
                    id = idx + 1,
                    roundId = round.id,
                    player = player,
                    hand = Hand(),
                    rollCount = 0,
                    state = TurnState.IN_PROGRESS,
                    score = null,
                ),
            )
        }
    }

    override fun getById(id: Int): Match? = matches.find { it.id == id }

    override fun getAll(): List<Match> = matches.toList()

    override fun save(entity: Match) {
        matches.removeIf { it.id == entity.id }
        matches.add(entity)
    }

    override fun deleteById(id: Int): Boolean = matches.removeIf { it.id == id }

    override fun clear() = matches.clear()
}
