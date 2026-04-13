package pt.isel.pdm.pokerDice.repo

import pt.isel.pdm.pokerDice.domain.Turn

interface RepositoryTurns : Repository<Turn> {
    fun createTurn(turn: Turn): Turn
}
