package pt.isel.daw.repo

import pt.isel.daw.Turn

interface RepositoryTurns : Repository<Turn> {
    fun createTurn(turn: Turn): Turn
}
