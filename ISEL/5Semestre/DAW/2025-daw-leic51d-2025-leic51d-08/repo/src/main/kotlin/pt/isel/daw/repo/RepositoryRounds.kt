package pt.isel.daw.repo

import pt.isel.daw.Round

interface RepositoryRounds : Repository<Round> {
    fun createRound(round: Round): Round
}
