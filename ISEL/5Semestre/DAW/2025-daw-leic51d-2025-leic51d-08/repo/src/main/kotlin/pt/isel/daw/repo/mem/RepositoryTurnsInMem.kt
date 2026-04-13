package pt.isel.daw.repo.mem

import pt.isel.daw.Turn
import pt.isel.daw.repo.RepositoryTurns

class RepositoryTurnsInMem : RepositoryTurns {
    private val turns = mutableListOf<Turn>()

    override fun createTurn(turn: Turn): Turn {
        val id = turns.size + 1
        return Turn(
            id = id,
            roundId = turn.roundId,
            player = turn.player,
            hand = turn.hand,
            rollCount = turn.rollCount,
            state = turn.state,
            score = turn.score,
        ).also { turns.add(it) }
    }

    override fun getById(id: Int): Turn? = turns.find { it.id == id }

    override fun getAll(): List<Turn> = turns.toList()

    override fun save(entity: Turn) {
        turns.removeIf { it.id == entity.id }
        turns.add(entity)
    }

    override fun deleteById(id: Int): Boolean = turns.removeIf { it.id == id }

    override fun clear() = turns.clear()
}
