package pt.isel.daw.repo.mem

import pt.isel.daw.Hand
import pt.isel.daw.Lobby
import pt.isel.daw.Match
import pt.isel.daw.Round
import pt.isel.daw.Turn
import pt.isel.daw.TurnState
import pt.isel.daw.User
import pt.isel.daw.repo.RepositoryMatches

class RepositoryMatchesInMem : RepositoryMatches {
    private val matches = mutableListOf<Match>()
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
            val round = repoRound.createRound(Round(idx, matchId, rnum, mutableListOf(), lobby.players[0]))
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
