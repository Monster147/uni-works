package pt.isel.pdm.pokerDice.repo

import pt.isel.pdm.pokerDice.domain.Round

interface RepositoryRounds : Repository<Round> {
    fun createRound(round: Round): Round
}
