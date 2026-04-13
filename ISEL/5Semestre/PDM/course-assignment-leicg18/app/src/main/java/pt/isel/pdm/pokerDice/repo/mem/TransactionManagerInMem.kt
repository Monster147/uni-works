package pt.isel.pdm.pokerDice.repo.mem

import pt.isel.pdm.pokerDice.repo.Transaction
import pt.isel.pdm.pokerDice.repo.TransactionManager


class TransactionManagerInMem : TransactionManager {
    private val repoUsers = RepositoryUserInMem()
    private val repoLobbies = RepositoryLobbiesInMem()
    private val repoMatches = RepositoryMatchesInMem()
    private val repoRounds = RepositoryRoundsInMem()
    private val repoTurns = RepositoryTurnsInMem()
    private val repoInvitations = RepositoryInvitationInMem()

    override suspend fun <R> run(block: suspend Transaction.() -> R): R =
        block(TransactionInMem(repoUsers, repoLobbies, repoMatches, repoRounds, repoTurns, repoInvitations))
}
