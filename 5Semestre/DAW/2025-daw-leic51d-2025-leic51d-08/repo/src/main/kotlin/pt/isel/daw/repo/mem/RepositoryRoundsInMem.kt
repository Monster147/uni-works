package pt.isel.daw.repo.mem

import pt.isel.daw.Round
import pt.isel.daw.repo.RepositoryRounds

class RepositoryRoundsInMem : RepositoryRounds {
    private val rounds = mutableListOf<Round>()

    override fun createRound(round: Round): Round {
        val roundId = rounds.size + 1
        val matchId = round.matchId
        return Round(
            id = roundId,
            matchId = matchId,
            roundNumber = round.roundNumber,
            turns = mutableListOf(),
            currentPlayer = round.currentPlayer,
        ).also { rounds.add(it) }
    }

    override fun getById(id: Int): Round? = rounds.find { it.id == id }

    override fun getAll(): List<Round> = rounds.toList()

    override fun save(entity: Round) {
        rounds.removeIf { it.id == entity.id }
        rounds.add(entity)
    }

    override fun deleteById(id: Int): Boolean = rounds.removeIf { it.id == id }

    override fun clear() = rounds.clear()
}
